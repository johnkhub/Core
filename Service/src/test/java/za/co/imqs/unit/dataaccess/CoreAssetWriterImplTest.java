package za.co.imqs.unit.dataaccess;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import za.co.imqs.coreservice.dataaccess.CoreAssetReader;
import za.co.imqs.coreservice.dataaccess.CoreAssetReaderImpl;
import za.co.imqs.coreservice.dataaccess.CoreAssetWriter;
import za.co.imqs.coreservice.dataaccess.CoreAssetWriterImpl;
import za.co.imqs.coreservice.dataaccess.exception.AlreadyExistsException;
import za.co.imqs.coreservice.dataaccess.exception.NotFoundException;
import za.co.imqs.coreservice.dataaccess.exception.ValidationFailureException;
import za.co.imqs.coreservice.dto.asset.QuantityDto;
import za.co.imqs.coreservice.model.AssetEnvelope;
import za.co.imqs.coreservice.model.CoreAsset;
import za.co.imqs.libimqs.dbutils.HikariCPClientConfigDatasourceHelper;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static za.co.imqs.TestUtils.BOING;
import static za.co.imqs.TestUtils.SERVICES;
import static za.co.imqs.TestUtils.ServiceRegistry.PG;
import static za.co.imqs.coreservice.model.ORM.getTableName;

/**
 * (c) 2020 IMQS Software
 * <p>
 * User: frankvr
 * Date: 2020/02/06
 */
@Slf4j
public class CoreAssetWriterImplTest {

    private static final UUID THE_ASSET_ID = UUID.fromString("46514cb4-c4a1-4ef2-a76c-c2b16f4cdbaa");
    private static final UUID V6_EXT_ID = UUID.fromString("c6a74a62-54f5-4f93-adf3-abebab3d3467");

    private final JdbcTemplate jdbc;

    @Rule
    public ExpectedException expect = ExpectedException.none();


    public CoreAssetWriterImplTest() {
        this.jdbc = new JdbcTemplate(
                HikariCPClientConfigDatasourceHelper.getDefaultDataSource(
                        "jdbc:postgresql://"+SERVICES.get(PG)+":5432/test_core","postgres","1mq5p@55w0rd"
                )
        );

        DbCreator.create(jdbc.getDataSource());
    }

    @Before
    public void clear() {
        clearAsset(THE_ASSET_ID);
    }

    @Test
    public void addNewAllFields() {
        final CoreAsset expected = getObject();

        final CoreAsset underTest = new CoreAsset();
        underTest.setSerial_number(expected.getSerial_number());
        underTest.setName(expected.getName());
        underTest.setLongitude(expected.getLongitude());
        underTest.setLatitude(expected.getLatitude());
        underTest.setGeom(expected.getGeom());
        underTest.setFunc_loc_path(expected.getFunc_loc_path());
        underTest.setCode(expected.getCode());
        underTest.setBarcode(expected.getBarcode());
        underTest.setAsset_type_code(expected.getAsset_type_code());
        underTest.setAsset_id(expected.getAsset_id());
        underTest.setAdm_path(expected.getAdm_path());
        underTest.setCreation_date(expected.getCreation_date());
        underTest.setDeactivated_at(expected.getDeactivated_at());

        final CoreAssetReader reader = new CoreAssetReaderImpl(jdbc.getDataSource());
        final CoreAssetWriter writer = new CoreAssetWriterImpl(jdbc.getDataSource(), BOING, TASK_SCHEDULER);
        writer.createAssets(Collections.singletonList(underTest));
        assertEquals(expected, reader.getAsset(expected.getAsset_id()));
    }

    @Test
    public void addNewAllFieldsEnvelope() {
        final CoreAsset expected = getObject();

        final AssetEnvelope underTest = new AssetEnvelope();
        underTest.setSerial_number(expected.getSerial_number());
        underTest.setName(expected.getName());
        underTest.setLongitude(expected.getLongitude());
        underTest.setLatitude(expected.getLatitude());
        underTest.setGeom(expected.getGeom());
        underTest.setFunc_loc_path(expected.getFunc_loc_path());
        underTest.setCode(expected.getCode());
        underTest.setBarcode(expected.getBarcode());
        underTest.setAsset_type_code(expected.getAsset_type_code());
        underTest.setAsset_id(expected.getAsset_id());
        underTest.setAdm_path(expected.getAdm_path());
        underTest.setCreation_date(expected.getCreation_date());
        underTest.setDeactivated_at(expected.getDeactivated_at());


        //jdbc.update("INSERT INTO asset.ref_district (k,v) VALUES ('WESTCOAST','West Coast')");
        //jdbc.update("INSERT INTO asset.ref_town (k,v) VALUES ('ATLANTIS','Atlantis')");

        underTest.setDistrict_code("WESTCOAST");
        underTest.setTown_code("ATLANTIS");

        final CoreAssetReader reader = new CoreAssetReaderImpl(jdbc.getDataSource());
        final CoreAssetWriter writer = new CoreAssetWriterImpl(jdbc.getDataSource(), BOING, TASK_SCHEDULER);
        writer.createAssets(Collections.singletonList(underTest));
        assertEquals(expected, reader.getAsset(expected.getAsset_id()));
    }

    @Test
    public void addExisting() {
        expect.expect(AlreadyExistsException.class);

        addNewAllFields();
        addNewAllFields();
    }

    @Test
    public void addNewNoPermission() {
        fail("Not implemented");
        //expect.expect(NotPermittedException.class);
    }

    @Test
    public void updateExisting() {
        addNewAllFields();

        final CoreAssetReader reader = new CoreAssetReaderImpl(jdbc.getDataSource());
        final CoreAsset expected = reader.getAsset(THE_ASSET_ID);
        expected.setBarcode("abcd");

        final CoreAsset underTest = reader.getAsset(THE_ASSET_ID);
        underTest.setBarcode(expected.getBarcode());
        underTest.setAsset_id(expected.getAsset_id());
        underTest.setIs_owned(false);

        final CoreAssetWriter writer = new CoreAssetWriterImpl(jdbc.getDataSource(),BOING, TASK_SCHEDULER);
        writer.updateAssets(Collections.singletonList(underTest));
        assertEquals(expected, reader.getAsset(expected.getAsset_id()));
    }

    @Test
    public void updateNonExisting() {
        expect.expect(NotFoundException.class);
        final CoreAssetWriter writer = new CoreAssetWriterImpl(jdbc.getDataSource(),BOING, TASK_SCHEDULER);
        writer.updateAssets(Collections.singletonList(getObject()));
    }

    @Test
    public void deleteExisting() {
        expect.expect(UnsupportedOperationException.class);

        addNewAllFields();
        final CoreAssetWriter writer = new CoreAssetWriterImpl(jdbc.getDataSource(),BOING, TASK_SCHEDULER);
        writer.deleteAssets(Collections.singletonList(getObject().getAsset_id()));
    }

    @Test
    public void deleteNonExisting() {
        expect.expect(UnsupportedOperationException.class);

        //expect.expect(NotFoundException.class);
        final CoreAssetWriter writer = new CoreAssetWriterImpl(jdbc.getDataSource(),BOING, TASK_SCHEDULER);
        writer.deleteAssets(Collections.singletonList(getObject().getAsset_id()));
    }


    @Test
    public void addInvalid() {
        expect.expect(ValidationFailureException.class);
        final CoreAssetWriter writer = new CoreAssetWriterImpl(jdbc.getDataSource(),BOING, TASK_SCHEDULER);
        final CoreAsset asset = getObject();
        asset.setAsset_id(null);
        writer.createAssets(Collections.singletonList(asset));
    }

    @Test
    public void updateInvalid() {
        expect.expect(ValidationFailureException.class);
        final CoreAssetWriter writer = new CoreAssetWriterImpl(jdbc.getDataSource(),BOING, TASK_SCHEDULER);
        final CoreAsset asset = getObject();
        asset.setAsset_id(null);
        writer.updateAssets(Collections.singletonList(asset));
    }

    @Test
    public void addExternalLink() {
        final CoreAssetWriter writer = new CoreAssetWriterImpl(jdbc.getDataSource(),BOING, TASK_SCHEDULER);
        final CoreAsset asset = getObject();
        writer.createAssets(Collections.singletonList(asset));
        final String link = UUID.randomUUID().toString();
        writer.addExternalLink(asset.getAsset_id(), V6_EXT_ID, link);
        assertEquals(link, getExternalLink(asset.getAsset_id(), V6_EXT_ID));
    }

    @Test
    public void deleteExternalLink() {
        final CoreAssetWriter writer = new CoreAssetWriterImpl(jdbc.getDataSource(),BOING, TASK_SCHEDULER);
        final CoreAsset asset = getObject();
        writer.createAssets(Collections.singletonList(asset));
        final String link = UUID.randomUUID().toString();
        writer.addExternalLink(asset.getAsset_id(), V6_EXT_ID, link);
        assertEquals(link, getExternalLink(asset.getAsset_id(), V6_EXT_ID));

        writer.deleteExternalLink(asset.getAsset_id(), V6_EXT_ID, link);
        assertNull(getExternalLink(asset.getAsset_id(), V6_EXT_ID));
    }

    @Test
    public void addExternalLinkToUnknownType() {
        expect.expect(ValidationFailureException.class);

        final CoreAssetWriter writer = new CoreAssetWriterImpl(jdbc.getDataSource(),BOING, TASK_SCHEDULER);
        final CoreAsset asset = getObject();
        writer.createAssets(Collections.singletonList(asset));
        writer.addExternalLink(asset.getAsset_id(),  UUID.randomUUID(), UUID.randomUUID().toString());
    }

    @Test
    public void addExternalLinkToNonExistantAsset() {
        expect.expect(ValidationFailureException.class);

        final CoreAssetWriter writer = new CoreAssetWriterImpl(jdbc.getDataSource(),BOING, TASK_SCHEDULER);
        final String link = UUID.randomUUID().toString();
        final UUID fakeAsset = UUID.randomUUID();
        writer.addExternalLink(fakeAsset, V6_EXT_ID, link);
        assertEquals(link, getExternalLink(fakeAsset, V6_EXT_ID));
    }

    private void clearAsset(UUID uuid) {
       new CoreAssetWriterImpl(jdbc.getDataSource(), BOING, TASK_SCHEDULER).obliterateAssets(uuid);
    }


    private String getExternalLink(UUID asset, UUID type) {
        try {
            return jdbc.queryForObject("SELECT external_id FROM asset_link WHERE asset_id = ? AND external_id_type = ?", String.class, asset, type);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    private CoreAsset getObject() {
        final CoreAsset expected = new CoreAsset();
        expected.setSerial_number("RA1234-234-6");
        expected.setName("Cat flap 12");
        expected.setLongitude(new BigDecimal("12.345600"));
        expected.setLatitude(new BigDecimal("-13.786800"));
        expected.setGeom("MULTIPOLYGON Z (((22.4258028183888 -33.9756669130654 0,22.426934082936 -33.9789624951734 0,22.4273665570524 -33.9802223620662 0,22.4278365423765 -33.9815918141869 0,22.4289391261444 -33.9848017717944 0,22.4247519201511 -33.9864541525548 0,22.4219633201168 -33.9872360846769 0,22.4195504406938 -33.9878006482106 0,22.4177411714067 -33.9878909790908 0,22.4164165429352 -33.987957090472 0,22.4159907252105 -33.9878272112761 0,22.4159109729944 -33.9876290616083 0,22.4157336482681 -33.9871523539958 0,22.4155905782846 -33.986839563925 0,22.4153979371651 -33.9865420255672 0,22.4151060844692 -33.9861490425805 0,22.4150883230716 -33.9861214271615 0,22.4149781969254 -33.9859506080567 0,22.4149038270143 -33.9858057330741 0,22.4148332742697 -33.9855157925276 0,22.4147989479011 -33.9853364704447 0,22.4147206943231 -33.9852296323283 0,22.4146825507026 -33.9851764562104 0,22.4145112412653 -33.9849982371357 0,22.4143712513307 -33.9848486080701 0,22.4142414383609 -33.9846965338746 0,22.4137994642874 -33.9840166273879 0,22.4133283259987 -33.9833185193767 0,22.4132881602696 -33.9832390917716 0,22.4131280635085 -33.9829219961159 0,22.4129773342558 -33.9827119408029 0,22.4128343297902 -33.982513833324 0,22.4127427184551 -33.9823649308668 0,22.4126989312653 -33.9822760537289 0,22.4125978394047 -33.9820712864536 0,22.4124564143109 -33.9818504311656 0,22.4123001429855 -33.9816936836255 0,22.4122276614807 -33.9815525381541 0,22.4120483643068 -33.9813121925433 0,22.4118156723179 -33.9809535952092 0,22.4117393600996 -33.9808734586194 0,22.4114322625329 -33.9807094546667 0,22.4113588232503 -33.9806876183735 0,22.4112603257219 -33.9806584592391 0,22.4111880892015 -33.9806369498304 0,22.4108002495979 -33.9805415837068 0,22.4103355186068 -33.9804272680745 0,22.4098796694244 -33.9802862168523 0,22.4097136800855 -33.980213521935 0,22.4093455359555 -33.9800534583834 0,22.4091700304518 -33.9799807563842 0,22.4089125234546 -33.979904652612 0,22.4085558978339 -33.9798054403722 0,22.4084529164263 -33.9797587449992 0,22.4084522574871 -33.979758492993 0,22.4083099747725 -33.9797458614686 0,22.4081345964982 -33.9797385620479 0,22.4079859006306 -33.9797278197436 0,22.4078506249159 -33.9797200386122 0,22.4076611981001 -33.9796832170873 0,22.4075680240599 -33.9796654392341 0,22.4073936737014 -33.9796351364805 0,22.4069487217477 -33.9794932313714 0,22.4065576335117 -33.9793352366259 0,22.4063411365399 -33.9792135746992 0,22.4061787935744 -33.9791264489297 0,22.4060167761363 -33.9790257999241 0,22.4058818925663 -33.97890863249 0,22.4058313408497 -33.9788667885078 0,22.4057342203291 -33.9787799269424 0,22.4056585498771 -33.9786940025369 0,22.4056585426914 -33.9786938225495 0,22.4055327286766 -33.9783974781442 0,22.4054095766856 -33.9781000687913 0,22.4053810747088 -33.9779251765539 0,22.4053663495477 -33.9776321506805 0,22.4054759929691 -33.9771956741023 0,22.4055030352282 -33.9771091560503 0,22.4054920879537 -33.977081440345 0,22.4054813639162 -33.97705389864 0,22.4055777070338 -33.9768341945723 0,22.405685928965 -33.9766086656165 0,22.4058308629843 -33.9763844617646 0,22.4061780923259 -33.9759237429593 0,22.4063792729397 -33.9756647354281 0,22.4066263108682 -33.9754438274388 0,22.4066677688402 -33.9753988030103 0,22.4067091151558 -33.9753536915703 0,22.4070379002561 -33.9749918635988 0,22.4074757004864 -33.9745031346025 0,22.4077075593237 -33.9742299408504 0,22.4077750963016 -33.9741177042744 0,22.4077826295549 -33.97410596349 0,22.407864612731 -33.9739817944324 0,22.4079694422429 -33.9737906832889 0,22.4080324037065 -33.9736559600438 0,22.4080753422931 -33.9735749469331 0,22.4081608659054 -33.9734203181207 0,22.4083051759591 -33.9731455866625 0,22.4083947937379 -33.9729148055022 0,22.4084072479295 -33.9728584221803 0,22.408422280793 -33.9727907957944 0,22.4084305050132 -33.9727232681074 0,22.4085015287427 -33.9725548965148 0,22.4085171281876 -33.9725231115134 0,22.4086145367143 -33.9722870689674 0,22.4087797809892 -33.9720059004688 0,22.4088539664563 -33.9718706854229 0,22.4089147326716 -33.9717352118297 0,22.4089514009702 -33.9716569850469 0,22.4089878567726 -33.971578854241 0,22.4090464100025 -33.9714773170145 0,22.4090779342936 -33.9713758987145 0,22.4090978654584 -33.9713307498137 0,22.4091679982285 -33.9711509607634 0,22.4092378102861 -33.9709712706673 0,22.4092771357971 -33.9708810778009 0,22.4093166808553 -33.9707909689278 0,22.4093463089998 -33.9707232980228 0,22.4093921570999 -33.9705879677052 0,22.4094550159725 -33.970475139334 0,22.409499177613 -33.9703625592485 0,22.4095635902927 -33.9702047312193 0,22.409603455232 -33.9701145233022 0,22.4096867877682 -33.9699566209016 0,22.4097902180612 -33.9697875299745 0,22.4097966326056 -33.9697423955337 0,22.4098106572964 -33.9697199336732 0,22.4098158061948 -33.9697134843254 0,22.4098759805756 -33.9696228030678 0,22.4108632340732 -33.9695797373845 0,22.4109088944072 -33.9695777496882 0,22.411229261936 -33.9695635443809 0,22.4119521719273 -33.9695323098863 0,22.4139943681448 -33.9694436267229 0,22.4142417757744 -33.9694328813211 0,22.4149814057521 -33.9694007141623 0,22.4186086472322 -33.9692516037636 0,22.4214966537201 -33.9698837077632 0,22.4231586001184 -33.9718261448209 0,22.4236837982338 -33.9725885673332 0,22.4266607903055 -33.9712269294298 0,22.4276661478726 -33.974019336716 0,22.4276928041401 -33.9740933701833 0,22.4278716186297 -33.9745901989766 0,22.4274074657797 -33.9747887329153 0,22.4257009211204 -33.9755180371121 0,22.4258028183888 -33.9756669130654 0),(22.4190162332604 -33.9774345730208 0,22.4230994323383 -33.9722084342527 0,22.425668134788 -33.9757249025405 0,22.423099428728 -33.9722083442605 0,22.4190162332604 -33.9774345730208 0),(22.4191826592467 -33.9730410715013 0,22.416730664398 -33.9762061064154 0,22.4167307161649 -33.9762061342404 0,22.4191826592467 -33.9730410715013 0)))");
        expected.setFunc_loc_path("a.b.c"); // this should actually fail
        expected.setCode("c");
        expected.setBarcode("12342346");
        expected.setAsset_type_code("ROOM");
        expected.setAsset_id(THE_ASSET_ID);
        expected.setAdm_path("x.y.z");
        expected.setCreation_date(new Timestamp(System.currentTimeMillis()));
        expected.setDeactivated_at(null);
        expected.setIs_owned(false);
        return expected;
    }

    private final TaskScheduler TASK_SCHEDULER = new TaskScheduler() {
        @Override
        public ScheduledFuture<?> schedule(Runnable runnable, Trigger trigger) {
            return null;
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable runnable, Date date) {
            return null;
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, Date date, long l) {
            return null;
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long l) {
            return null;
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable runnable, Date date, long l) {
            return null;
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable runnable, long l) {
            return null;
        }
    };

    @Test
    public void testIt() {
        final QuantityDto quantity = new QuantityDto();
        quantity.setAsset_id(UUID.randomUUID());
        quantity.setName("nom");
        final StringBuffer sql = new StringBuffer("UPDATE public.quantity ");
        final BeanPropertySqlParameterSource params = new BeanPropertySqlParameterSource(quantity);
        for (String name : params.getParameterNames()) {
            if (params.getValue(name) != null) {
                sql.append("\n SET ").append(name).append(" = :").append(name);
            }
        }
        log.info(sql.toString());
    }
}