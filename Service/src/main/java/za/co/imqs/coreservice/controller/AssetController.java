package za.co.imqs.coreservice.controller;

import filter.FilterBuilder;
import filter.Modifiers;
import filter.SqlWhereLexer;
import filter.SqlWhereParser;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.imqs.coreservice.audit.AuditLogEntry;
import za.co.imqs.coreservice.audit.AuditLogger;
import za.co.imqs.coreservice.audit.AuditLoggingProxy;
import za.co.imqs.coreservice.auth.authorization.AssetACLPolicy;
import za.co.imqs.coreservice.auth.authorization.DtpwAclPolicy;
import za.co.imqs.coreservice.dataaccess.CoreAssetReader;
import za.co.imqs.coreservice.dataaccess.CoreAssetWriter;
import za.co.imqs.coreservice.dataaccess.PermissionRepository;
import za.co.imqs.coreservice.dataaccess.exception.ValidationFailureException;
import za.co.imqs.coreservice.dto.asset.CoreAssetDto;
import za.co.imqs.coreservice.model.AssetFactory;
import za.co.imqs.coreservice.model.CoreAsset;
import za.co.imqs.coreservice.model.ORM;
import za.co.imqs.services.ThreadLocalUser;
import za.co.imqs.services.UserContext;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

import static za.co.imqs.coreservice.ServiceConfiguration.Features.AUTHORISATION_GLOBAL;
import static za.co.imqs.coreservice.WebMvcConfiguration.ASSET_ROOT_PATH;
import static za.co.imqs.coreservice.audit.AuditLogEntry.of;
import static za.co.imqs.coreservice.controller.ExceptionRemapper.mapException;
import static za.co.imqs.coreservice.dataaccess.PermissionRepository.*;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_PRODUCTION;
import static za.co.imqs.spring.service.webap.DefaultWebAppInitializer.PROFILE_TEST;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/05
 */
@SuppressWarnings("rawtypes")
@Profile({PROFILE_PRODUCTION, PROFILE_TEST})
@RestController
@Slf4j
@RequestMapping(ASSET_ROOT_PATH)
public class AssetController {
    private static final long MAX_RESULT_ROWS = 10000;

    private final CoreAssetWriter assetWriter;
    private final CoreAssetReader assetReader;

    private final AssetFactory aFact = new AssetFactory();
    private final AuditLoggingProxy audit;

    private final PermissionRepository perms;
    private final AssetACLPolicy assetAclPolicy;

    @Autowired
    public AssetController(
            CoreAssetWriter assetWriter,
            CoreAssetReader assetReader,
            AuditLogger auditLogger,
            PermissionRepository perms
    ) {
        this.assetWriter = assetWriter;
        this.assetReader = assetReader;
        this.audit = new AuditLoggingProxy(auditLogger);
        this.perms = perms;
        this.assetAclPolicy = new DtpwAclPolicy(perms, assetReader);
    }

    @RequestMapping(
            method = RequestMethod.PUT, value = "/{uuid}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity addAsset(
            @PathVariable UUID uuid,
            @RequestBody CoreAssetDto asset,
            @RequestParam(required = false, defaultValue ="false", name="testRun") boolean testRun
    ) {
        final UserContext user = ThreadLocalUser.get();
        // Authorisation
        try {
            audit.tryIt(
                    new AuditLogEntry(user.getUserUuid(), AuditLogger.Operation.ADD_ASSET, of("asset", uuid)).setCorrelationId(uuid),
                    () -> {
                        expectAllowCreate(user.getUserUuid(), asset);
                        final CoreAsset assetModel = aFact.create(uuid, asset);
                        assetWriter.createAssets(Collections.singletonList(assetModel));
                        if (AUTHORISATION_GLOBAL.isActive()) {
                            assetAclPolicy.onCreateEntity(user.getUserUuid(), user.getUserUuid(), assetModel);
                        }
                        return null;
                    }
            );
            return new ResponseEntity(HttpStatus.CREATED);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.DELETE, value = "/{uuid}"
    )
    public ResponseEntity deleteAsset(@PathVariable UUID uuid) {
        final UserContext user = ThreadLocalUser.get();
        // Authorisation
        try {
            final Map<String,UUID> p = new HashMap<>();
            audit.tryIt(
                    new AuditLogEntry(user.getUserUuid(), AuditLogger.Operation.DELETE_ASSET, of("asset", uuid)).setCorrelationId(uuid),
                    () -> {
                        perms.expectPermission(user.getUserUuid(), uuid, PERM_DELETE);
                        assetWriter.deleteAssets(Collections.singletonList(uuid));
                        if (AUTHORISATION_GLOBAL.isActive()) {
                            assetAclPolicy.onDeleteEntity(user.getUserUuid(), user.getUserUuid(),uuid);
                        }
                        return null;
                    }
            );
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.PATCH, value = "/{uuid}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity updateAsset(@PathVariable UUID uuid, @RequestBody CoreAssetDto asset) {
        final UserContext user = ThreadLocalUser.get();
        // Authorisation
        try {
            audit.tryIt(
                    new AuditLogEntry(user.getUserUuid(), AuditLogger.Operation.UPDATE_ASSET, of("asset", uuid)).setCorrelationId(uuid),
                    () -> {
                        perms.expectPermission(user.getUserUuid(), uuid, PERM_UPDATE);
                        assetWriter.updateAssets(Collections.singletonList(aFact.update(uuid, asset)));
                        return null;
                    }
            );
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }


    @RequestMapping(
            method = RequestMethod.PUT, value = "/link/{uuid}/to/{external_id_type}/{external_id}"
    )
    public ResponseEntity addExternalLink(@PathVariable UUID uuid, @PathVariable UUID external_id_type, @PathVariable String external_id) {
        final UserContext user = ThreadLocalUser.get();
        // Authorisation
        try {
            audit.tryIt(
                    new AuditLogEntry(user.getUserUuid(), AuditLogger.Operation.ADD_ASSET_LINK, of("asset", uuid, "external_id_type", external_id_type, "external_id", external_id)).setCorrelationId(uuid),
                    () -> {
                        perms.expectPermission(user.getUserUuid(), uuid, PERM_UPDATE);
                        assetWriter.addExternalLink(uuid, external_id_type, external_id);
                        return null;
                    }
            );
            return new ResponseEntity(HttpStatus.CREATED);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.PATCH, value = "/link/{uuid}/to/{external_id_type}/{external_id}"
    )
    public ResponseEntity updateExternalLink(@PathVariable UUID uuid, @PathVariable UUID external_id_type, @PathVariable String external_id) {
        final UserContext user = ThreadLocalUser.get();
        // Authorisation
        try {
            audit.tryIt(
                    new AuditLogEntry(user.getUserUuid(), AuditLogger.Operation.UPDATE_ASSET_LINK, of("asset", uuid, "external_id_type", external_id_type, "external_id", external_id)).setCorrelationId(uuid),
                    () -> {
                        perms.expectPermission(user.getUserUuid(), uuid, PERM_UPDATE);
                        assetWriter.updateExternalLink(uuid, external_id_type, external_id);
                        return null;
                    }
            );
            return new ResponseEntity(HttpStatus.CREATED);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.DELETE, value = "/link/{uuid}/to/{external_id_type}/{external_id}"
    )
    public ResponseEntity deleteExternalLink(@PathVariable UUID uuid, @PathVariable UUID external_id_type, @PathVariable String external_id) {
        final UserContext user = ThreadLocalUser.get();
        // Authorisation
        try {
            audit.tryIt(
                    new AuditLogEntry(user.getUserUuid(), AuditLogger.Operation.DELETE_ASSET_LINK, of("asset", uuid, "external_id_type", external_id_type, "external_id", external_id)).setCorrelationId(uuid),
                    () -> {
                        perms.expectPermission(user.getUserUuid(), uuid, PERM_UPDATE);
                        assetWriter.deleteExternalLink(uuid, external_id_type, external_id);
                        return null;
                    }
            );
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.GET, value = "/link/{uuid}/to/{external_id_type}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity getExternalLink(@PathVariable UUID uuid, @PathVariable UUID external_id_type) {
        final UserContext user = ThreadLocalUser.get();
        try {
            // Read-permissions are enforced by exclusion from the result set
            return new ResponseEntity<>(assetReader.getExternalLink(uuid,external_id_type), HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.GET, value = "/link/types"
    )
    public ResponseEntity getExternalLinkTypes() {
        final UserContext user = ThreadLocalUser.get();
        // Authorisation
        try {
            // Read-permissions are enforced by exclusion from the result set
            return new ResponseEntity(assetReader.getExternalLinkTypes(), null, HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }





    @RequestMapping(
            method = RequestMethod.PUT, value = "/group/{uuid}/to/{grouping_id_type}/{grouping_id}"
    )
    public ResponseEntity addGroupingId(@PathVariable UUID uuid, @PathVariable UUID grouping_id_type, @PathVariable String grouping_id) {
        final UserContext user = ThreadLocalUser.get();
        // Authorisation
        try {
            audit.tryIt(
                    new AuditLogEntry(user.getUserUuid(), AuditLogger.Operation.ADD_ASSET_GROUPING, of("asset", uuid, "grouping_id_type", grouping_id_type, "grouping_id", grouping_id)).setCorrelationId(uuid),
                    () -> {
                        assetWriter.addToGrouping(uuid, grouping_id_type, grouping_id);
                        return null;
                    }
            );
            return new ResponseEntity(HttpStatus.CREATED);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.PATCH, value = "/group/{uuid}/to/{grouping_id_type}/{grouping_id}"
    )
    public ResponseEntity updateGroupingId(@PathVariable UUID uuid, @PathVariable UUID grouping_id_type, @PathVariable String grouping_id) {
        final UserContext user = ThreadLocalUser.get();
        // Authorisation
        try {
            audit.tryIt(
                    new AuditLogEntry(user.getUserUuid(), AuditLogger.Operation.UPDATE_ASSET_GROUPING, of("asset", uuid, "grouping_id_type", grouping_id_type, "grouping_id", grouping_id)).setCorrelationId(uuid),
                    () -> {
                        assetWriter.updateGrouping(uuid, grouping_id_type, grouping_id);
                        return null;
                    }
            );
            return new ResponseEntity(HttpStatus.CREATED);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.DELETE, value = "/group/{uuid}/to/{grouping_id_type}/{grouping_id}"
    )
    public ResponseEntity deleteGroupingId(@PathVariable UUID uuid, @PathVariable UUID grouping_id_type, @PathVariable String grouping_id) {
        final UserContext user = ThreadLocalUser.get();
        // Authorisation
        try {
            audit.tryIt(
                    new AuditLogEntry(user.getUserUuid(), AuditLogger.Operation.DELETE_ASSET_GROUPING, of("asset", uuid, "grouping_id_type", grouping_id_type, "grouping_id", grouping_id)).setCorrelationId(uuid),
                    () -> {
                        assetWriter.deleteFromGrouping(uuid, grouping_id_type, grouping_id);
                        return null;
                    }
            );
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.GET, value = "/group/{uuid}/to/{grouping_id_type}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity getGroupingId(@PathVariable UUID uuid, @PathVariable UUID grouping_id_type) {
        final UserContext user = ThreadLocalUser.get();
        try {
            return new ResponseEntity<>(assetReader.getGrouping(uuid,grouping_id_type), HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.GET, value = "/group/types"
    )
    public ResponseEntity getGroupingTypes() {
        final UserContext user = ThreadLocalUser.get();
        // Authorisation
        try {
            return new ResponseEntity(assetReader.getGroupingTypes(), null, HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }





    @RequestMapping(
            method = RequestMethod.GET, value = "/{uuid}"
    )
    public ResponseEntity<?> get(@PathVariable UUID uuid) {
        final UserContext user = ThreadLocalUser.get();
        try {
            // Read-permissions are enforced by exclusion from the result set
            return new ResponseEntity<>(asDto(assetReader.getAsset(uuid)), null, HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.GET, value = "/query"
    )
    public ResponseEntity getWithFilter(@RequestParam Map<String, String> paramMap) {
        final UserContext user = ThreadLocalUser.get();
        try {
            final Map<String,String> sanitisedMap = new HashMap<>();
            for (Map.Entry<String,String> e : paramMap.entrySet()) {
                sanitisedMap.put(e.getKey().trim().toLowerCase(), e.getValue());
            }
            
            final FilterBuilder filter = parse(sanitisedMap.get("filter"));
            
            final String orderBy = sanitisedMap.get(Modifiers.ORDER_BY);
            if (StringUtils.isNotEmpty(orderBy)) {
                final String[] fields = orderBy.split(",");
                String direction = fields[fields.length - 1].trim().toLowerCase();
                int len = direction.length();
                if (direction.endsWith(Modifiers.ASC)) {
                    fields[fields.length-1] = fields[fields.length-1].substring(0, len-3);
                    filter.orderBy(false, fields);
                } else if (direction.endsWith(Modifiers.DESC)) {
                    fields[fields.length-1] = fields[fields.length-1].substring(0, len-4);
                    filter.orderBy(true, fields);
                } else {
                    filter.orderBy( fields);
                }


            }

            /*
            final String groupBy = sanitisedMap.get(Modifiers.GROUP_BY);
            if (StringUtils.isNotEmpty(groupBy)) {
                filter.groupBy(sanitisedMap.get(Modifiers.GROUP_BY));
            }
             */

            if (sanitisedMap.get(Modifiers.OFFSET) != null) filter.offset(Long.parseLong(sanitisedMap.get(Modifiers.OFFSET)));
            if (sanitisedMap.get(Modifiers.LIMIT) != null) {
                filter.limit(Math.min(Long.parseLong(sanitisedMap.get(Modifiers.LIMIT)), MAX_RESULT_ROWS));
            } else {
                filter.limit(MAX_RESULT_ROWS);
            }

            // Read-permissions are enforced by exclusion from the result set
            final List<CoreAssetDto> dtos = new LinkedList<>();
            for (CoreAsset asset : assetReader.getAssetByFilter(filter)) {
                dtos.add(asDto(asset));
            }
            return new ResponseEntity(dtos, HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.GET, value = "/func_loc_path/{path}"
    )
    public ResponseEntity getByPath(@PathVariable String path) {
        final UserContext user = ThreadLocalUser.get();
        try {
            //expectPermission(user.getUserUuid(), uuid, PERM_READ generic read permissions?);
            return new ResponseEntity(asDto(assetReader.getAssetByFuncLocPath(path.replace("+","."))), null, HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.GET, value = "/grouped_by/{grouping_id_type}/{grouping_id}"
    )
    public ResponseEntity getByGroupinglId(@PathVariable UUID grouping_id_type, @PathVariable String grouping_id) {
        final UserContext user = ThreadLocalUser.get();
        try {
            final List<CoreAssetDto> dtos = new LinkedList<>();
            for (CoreAsset asset : assetReader.getAssetsByGroupingId(grouping_id_type, grouping_id)) {
                dtos.add(asDto(asset));
            }
            return new ResponseEntity(dtos, HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.GET, value = "/linked_to/{external_id_type}/{external_id}"
    )
    public ResponseEntity getByExternalId(@PathVariable UUID external_id_type, @PathVariable String external_id) {
        final UserContext user = ThreadLocalUser.get();
        try {
            // Read-permissions are enforced by exclusion from the result set
            return new ResponseEntity(asDto(assetReader.getAssetByExternalId(external_id_type, external_id)), null, HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.GET, value = "/landparcel/{uuid}/assets"
    )
    public ResponseEntity<?> getAssetsLinkedToLandparcel(@PathVariable UUID uuid) {
        final UserContext user = ThreadLocalUser.get();
        try {
            // Read-permissions are enforced by exclusion from the result set
            return new ResponseEntity<>(assetReader.getAssetsLinkedToLandParcel(uuid), null, HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.PUT, value = "/landparcel/{landparcel_id}/asset/{asset_id}"
    )
    public ResponseEntity<?> linkAssetToLandparcel(@PathVariable UUID landparcel_id, @PathVariable UUID asset_id) {
        final UserContext user = ThreadLocalUser.get();
        // Authorisation
        try {
            audit.tryIt(
                    new AuditLogEntry(user.getUserUuid(), AuditLogger.Operation.ADD_LANDPARCEL_ASSET_LINK, of("landparcel", landparcel_id, "asset", asset_id)).setCorrelationId(landparcel_id),
                    () -> {
                        perms.expectPermission(user.getUserUuid(), landparcel_id, PERM_UPDATE);
                        assetWriter.linkAssetToLandParcel(asset_id, landparcel_id);
                        return null;
                    }
            );
            return new ResponseEntity(HttpStatus.CREATED);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.DELETE, value = "/landparcel/{landparcel_id}/asset/{asset_id}"
    )
    public ResponseEntity<?> unlinkAssetFromLandparcel(@PathVariable UUID landparcel_id, @PathVariable UUID asset_id) {
        final UserContext user = ThreadLocalUser.get();
        // Authorisation
        try {
            audit.tryIt(
                    new AuditLogEntry(user.getUserUuid(), AuditLogger.Operation.DELETE_LANDPARCEL_ASSET_LINK, of("landparcel", landparcel_id, "asset", asset_id)).setCorrelationId(landparcel_id),
                    () -> {
                        perms.expectPermission(user.getUserUuid(), landparcel_id, PERM_UPDATE);
                        assetWriter.unlinkAssetFromLandParcel(asset_id, landparcel_id);
                        return null;
                    }
            );
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }


    @RequestMapping(
            method = RequestMethod.PUT, value = "/table/{table}/field/{field}/asset/{uuid}/value/{value}"
    )
    public ResponseEntity<?> addLinkedData(
            @PathVariable String table,
            @PathVariable String field,
            @PathVariable UUID uuid,
            @PathVariable String value
    ) {
        final UserContext user = ThreadLocalUser.get();
        // Authorisation
        try {
            audit.tryIt(
                    new AuditLogEntry(user.getUserUuid(), AuditLogger.Operation.UPDATE_ASSET, of("asset", uuid)).setCorrelationId(uuid),
                    () -> {
                        assetWriter.addLinkedData(table.replace("+","."), field, uuid, value);
                        return null;
                    }
            );
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.PATCH, value = "/table/{table}/field/{field}/asset/{uuid}/value/{value}"
    )
    public ResponseEntity<?> updateLinkedData(
            @PathVariable String table,
            @PathVariable String field,
            @PathVariable UUID uuid,
            @PathVariable String value
    ) {
        final UserContext user = ThreadLocalUser.get();
        // Authorisation
        try {
            audit.tryIt(
                    new AuditLogEntry(user.getUserUuid(), AuditLogger.Operation.UPDATE_ASSET, of("asset", uuid)).setCorrelationId(uuid),
                    () -> {
                        assetWriter.updateLinkedData(table.replace("+","."), field, uuid, value);
                        return null;
                    }
            );
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    @RequestMapping(
            method = RequestMethod.DELETE, value = "/table/{table}/field/{field}/asset/{uuid}"
    )
    public ResponseEntity<?> deleteLinkedData(
            @PathVariable String table,
            @PathVariable String field,
            @PathVariable UUID uuid
    ) {
        final UserContext user = ThreadLocalUser.get();
        // Authorisation
        try {
            audit.tryIt(
                    new AuditLogEntry(user.getUserUuid(), AuditLogger.Operation.UPDATE_ASSET, of("asset", uuid)).setCorrelationId(uuid),
                    () -> {
                        assetWriter.deleteLinkedData(table.replace("+","."), field, uuid);
                        return null;
                    }
            );
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return mapException(e);
        }
    }

    private static <T extends CoreAssetDto, S extends CoreAsset> T asDto(S model) {
        try {
            T targetDto = ORM.dtoFactory(model.getAsset_type_code());
            final Map<String,Method> setters = new HashMap<>();
            for (PropertyDescriptor p : Introspector.getBeanInfo(targetDto.getClass()).getPropertyDescriptors()) {
                setters.put(p.getName(), p.getWriteMethod());
            }

            for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(model.getClass()).getPropertyDescriptors()) {
                final Method getter = propertyDescriptor.getReadMethod();
                final Method setter = setters.get(propertyDescriptor.getName());

                if (getter != null && setter != null) {
                    Object o = getter.invoke(model);
                    if (o != null) {
                        if (o instanceof UUID || o instanceof Timestamp || o instanceof BigDecimal) {
                            o = o.toString();
                        }

                        try {
                            setter.invoke(targetDto, o);
                        } catch (Exception c) {
                            final String msg = "Invoking " +setter + " with " + o.getClass().getCanonicalName();
                            log.error(msg);
                            throw new RuntimeException(c.getMessage()+"."+ msg);
                        }
                    }
                }
            }

            return targetDto;

        }  catch(IntrospectionException|InvocationTargetException|IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    private void expectAllowCreate(UUID principal, CoreAssetDto dto) {
        if (AUTHORISATION_GLOBAL.isActive()) {
            final UUID type = assetReader.getAssetTypeUUIDs().get(dto.getAsset_type_code());
            if (type == null) throw new IllegalArgumentException();
            perms.expectPermission(principal, type, PERM_CREATE);
        }
    }

    private FilterBuilder parse(String text) {
        synchronized (PARSER) {
            try {
                LEXER.setInputStream(new ANTLRInputStream(text));
                LEXER.reset();
                PARSER.setTokenStream(new CommonTokenStream(LEXER));
                PARSER.reset();

                final SqlWhereParser.ParseContext ctx = PARSER.parse();
                if (ctx.exception != null)
                    throw ctx.exception;

                return ctx.value;
            } catch (ParseCancellationException p) {
                throw new ValidationFailureException(p.getMessage());
            }
        }
    }

    private static final SqlWhereLexer LEXER = new SqlWhereLexer(new ANTLRInputStream());
    private static final SqlWhereParser PARSER = new SqlWhereParser(new CommonTokenStream(LEXER));
    static {
        PARSER.addErrorListener(new ThrowingErrorListener());
        LEXER.addErrorListener(new ThrowingErrorListener());
    }

    private static class ThrowingErrorListener extends BaseErrorListener {

        @Override
        public void syntaxError(
                Recognizer<?, ?> recognizer,
                Object offendingSymbol,
                int line, int charPositionInLine,
                String msg, RecognitionException e
        ) throws ParseCancellationException {
            throw new ParseCancellationException("line " + line + ":" + charPositionInLine + " " + msg);
        }
    }
}
