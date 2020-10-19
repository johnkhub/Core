package za.co.imqs.api.filter;

import filter.FilterBuilder;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;
import filter.SqlWhereParser;
import filter.SqlWhereLexer;

import static filter.ExpressionFactory.*;

@Slf4j
public class FilterTest {

    @Test
    public void testBuilderNot() {
        final FilterBuilder filter = new FilterBuilder();
        filter.expression(isNotNull("func_loc_path").not());
        log.info(filter.build());
    }
/*
    @Test
    public void testBuilderAnd() {
        final FilterBuilder filter = new FilterBuilder();
        filter.expression(isNotNull("func_loc_path")).andOp().expression(eq("func_loc_path", 12L));
        log.info(filter.build());
    }

    @Test
    public void testBuilderOr() {
        final FilterBuilder filter = new FilterBuilder();
        filter.expression(isNotNull("func_loc_path")).orOp().expression(eq("func_loc_path", 12L)).orOp(neq("creation_date", "2019-08-08 12:33:44"));
        log.info(filter.build());
    }

    @Test
    public void testBuilderAndAnd() {
        final FilterBuilder filter = new FilterBuilder();
        filter.expression(isNotNull("func_loc_path")).andOp().expression(eq("func_loc_path", 12L));
        filter.andOp(gr("ref_count", 17L));
        filter.groupBy("func_loc_path").limit(50L);
        log.info(filter.build());
    }
*/
    /*
    @Test
    public void testBuilderFunc() {
        final FilterBuilder filter = new FilterBuilder();
        filter.andOp(eq(func("lower", "name"), "halllo"));
        log.info(filter.build());
    }


     */
    private static final SqlWhereLexer LEXER = new SqlWhereLexer(new ANTLRInputStream());
    private static final SqlWhereParser PARSER = new SqlWhereParser(new CommonTokenStream(LEXER));
    static {
        PARSER.setErrorHandler(new BailErrorStrategy());
    }

    private static synchronized FilterBuilder addWhere(String where) {
        ANTLRInputStream in = new ANTLRInputStream(where);
        LEXER.setInputStream(in);
        LEXER.reset();
        CommonTokenStream tokens = new CommonTokenStream(LEXER);


        PARSER.setTokenStream(tokens);
        PARSER.reset();

        SqlWhereParser.ParseContext ctx = PARSER.parse();
        if (ctx.exception != null)
            throw ctx.exception;

        return ctx.value;
    }


    @Test
    public void testParsing() {
        /*
        log.info(addWhere("(appels.vrug <> 'pere')").build());

        log.info(addWhere("(appels.vrug > 'pere')").build());

        log.info(addWhere("(appels.vrug = 'pere')").build());

        log.info(addWhere("NOT (appels.vrug > 'pere')").build());
*/
        //log.info(addWhere("(appels.vrug > 'pere') OR pere.vrug <> 'tamatie' AND NOT aaa.x ='piesang' ").build());

        //log.info(addWhere("name='Envelope 1' and asset_type_code = 'ENVELOPE' or asset_type_code = 'BUILDING' or asset_type_code = 'FLOOR' or asset_type_code = 'FACILITY'").build());
        //log.info(addWhere("name='Envelope 1' and (asset_type_code = 'ENVELOPE' or asset_type_code = 'BUILDING' or asset_type_code = 'FLOOR' or asset_type_code = 'FACILITY')").build());

        log.info(addWhere(
                "name='Envelope 1' and " +
                "(asset_type_code = 'ENVELOPE') or (asset_type_code = 'BUILDING') or (asset_type_code = 'FLOOR') or (asset_type_code = 'FACILITY') and " +
                "func_loc_path='nat'").build());

        FilterBuilder bob = addWhere(
                "name='Envelope 1' and " +
                        "(asset_type_code = 'FACILITY') and " +
                        "TAGS['AT_RISK','BLUE']");
        log.info(bob.build());
        log.info(String.join(",",bob.getFields()));
    }

    @Test
    public void like() {
        FilterBuilder bob = addWhere("name LIKE 'Bob'");
        log.info(bob.build());
    }
}

