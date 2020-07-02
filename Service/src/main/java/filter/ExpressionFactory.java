package filter;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import za.co.imqs.coreservice.dataaccess.exception.ValidationFailureException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressionFactory {

    public static final String IS = "IS";
    public static final String IS_NOT = "IS NOT";

    public enum RelationalOperator {
        EQ("="),
        NEQ("<>"),
        GR(">"),
        GEQ(">="),
        LE("<"),
        LEQ("<=");

        private final String op;

         RelationalOperator(String op) {
            this.op = op;
        }

        public String op() {
             return op;
        }
    }

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public static <T> Expression isNull(String field) {
        return new StringExpression(field, IS, null);
    }

    public static <T> Expression isNotNull(String field) {
        return new StringExpression(field, IS_NOT, null);
    }

    public static <T> Expression of(String field, RelationalOperator op, T value, Class type) {
        switch (type.getSimpleName()) {
            case "Long": return longCriterion(field, op, (Long)value);
            case "String": return stringCriterion(field, op, (String)value);
            case "BigDecimal": return bigdecimalCriterion(field, op, (BigDecimal)value);
           /* case "DateTime": return datetimeCriterion(field, op, (DateTime)value);*/
            case "Path": return pathCriterion(field, op, (String)value);
            case "Boolean": return booleanCriterion(field, op, (Boolean)value);
        }
        throw new ValidationFailureException(String.format("Unknown type %s in parsed expression", type.getSimpleName()));
    }

    public static Expression stringCriterion(String field, RelationalOperator op, String value) {
        return new StringExpression(field, op.op(), value);
    }

    public static Expression longCriterion(String field, RelationalOperator op, Long value) {
        return new LongExpression(field, op.op(), value);
    }

    /*
    public static Expression datetimeCriterion(String field, RelationalOperator op, DateTime value) {
        return new DateTimeExpression(field, op.op(), value);
    }
    */

    public static Expression bigdecimalCriterion(String field, RelationalOperator op, BigDecimal value) {
        return new BigDecimalExpression(field, op.op(), value);
    }

    public static Expression booleanCriterion(String field, RelationalOperator op, Boolean value) {
        return new BooleanExpression(field, op.op(), value);
    }

    public static Expression pathCriterion(String field, RelationalOperator op, String value) {
        return new PathExpression(field, op.op(), value);
    }


    // Number
    public static Expression gr(String field, Long value) {
        return longCriterion(field, RelationalOperator.GR, value);
    }

    public static Expression le(String field, Long value) {
        return longCriterion(field, RelationalOperator.LE, value);
    }

    public static Expression leq(String field, Long value) {
        return longCriterion(field, RelationalOperator.LEQ, value);
    }

    public static Expression geq(String field, Long value) {
        return longCriterion(field, RelationalOperator.GEQ, value);
    }

    public static Expression eq(String field, Long value) {
        return longCriterion(field, RelationalOperator.EQ, value);
    }

    public static Expression neq(String field, Long value) {
        return longCriterion(field, RelationalOperator.NEQ, value);
    }




    public static Expression gr(String field, BigDecimal value) {
        return bigdecimalCriterion(field, RelationalOperator.GR, value);
    }

    public static Expression le(String field, BigDecimal value) {
        return bigdecimalCriterion(field, RelationalOperator.LE, value);
    }

    public static Expression leq(String field, BigDecimal value) {
        return bigdecimalCriterion(field, RelationalOperator.LEQ, value);
    }

    public static Expression geq(String field, BigDecimal value) {
        return bigdecimalCriterion(field, RelationalOperator.GEQ, value);
    }

    public static Expression eq(String field, BigDecimal value) {
        return bigdecimalCriterion(field, RelationalOperator.EQ, value);
    }

    public static Expression neq(String field, BigDecimal value) {
        return bigdecimalCriterion(field, RelationalOperator.NEQ, value);
    }

    /*
    // DateTime
    public static Expression gr(String field, DateTime value) {
        return datetimeCriterion(field, RelationalOperator.GR, value);
    }

    public static Expression le(String field, DateTime value) {
        return datetimeCriterion(field, RelationalOperator.LE, value);
    }

    public static Expression leq(String field, DateTime value) {
        return datetimeCriterion(field, RelationalOperator.LEQ, value);
    }

    public static Expression geq(String field, DateTime value) {
        return datetimeCriterion(field, RelationalOperator.GEQ, value);
    }

    public static Expression eq(String field, DateTime value) {
        return datetimeCriterion(field, RelationalOperator.EQ, value);
    }

    public static Expression neq(String field, DateTime value) {
        return datetimeCriterion(field, RelationalOperator.NEQ, value);
    }
    */


    // String
    /*
    public static CriterionBuilder match(String field, String value) {
        ;
    }
    */
    public static Expression eq(String field, String value) {
        return stringCriterion(field, RelationalOperator.EQ, value);
    }

    public static Expression neq(String field, String value) {
        return stringCriterion(field, RelationalOperator.NEQ, value);
    }

    public static Expression eq(FunctionCall field, String value) {
        return stringCriterion(field.get(), RelationalOperator.EQ, value);
    }

    public static Expression neq(FunctionCall field, String value) {
        return stringCriterion(field.get(), RelationalOperator.NEQ, value);
    }


    //
    public static Expression eq(String field, Boolean value) {
        return booleanCriterion(field, RelationalOperator.EQ, value);
    }

    public static Expression neq(String field, Boolean value) {
        return booleanCriterion(field, RelationalOperator.NEQ, value);
    }

    public static FunctionCall func(String name, Object ...fields) {
        return func(name, Arrays.asList(fields));
    }

    public static FunctionCall func(String name, List<Object> fields) {
        return new FunctionCall(name, fields);
    }


    private static abstract class SimpleExpression<T> implements Expression {
        protected final T value;
        private final String operator;
        private final String field;
        private boolean not;

        public SimpleExpression(String field, String operator, T value) {
            this.field = field;
            this.operator = operator;
            this.value = value;
        }

        @Override
        public String toString() {
            return (not ? " NOT " : "") +  field + " " +  operator + " " + getValue();
        }

        public Expression not() {
            this.not = true;
            return this;
        }

        public abstract String getValue();
    }

    /*
    private static class DateTimeExpression extends SimpleExpression<DateTime> {

        public DateTimeExpression(String field, String operator, DateTime value) {
            super(field, operator, value);
        }

        @Override
        public String getValue() {
            return "'" + value.toString(DATETIME_FORMATTER)+ "'";
        }
    }
    */

    private static class LongExpression extends SimpleExpression<Long> {

        public LongExpression(String field, String operator, Long value) {
            super(field, operator, value);
        }

        @Override
        public String getValue() {
            return Long.toString(value);
        }
    }

    private static class BigDecimalExpression extends SimpleExpression<BigDecimal> {

        public BigDecimalExpression(String field, String operator, BigDecimal value) {
            super(field, operator, value);
        }

        @Override
        public String getValue() {
            return value.toPlainString();
        }
    }

    private static class BooleanExpression extends SimpleExpression<Boolean> {

        public BooleanExpression(String field, String operator, Boolean value) {
            super("coalesce("+field+",false)", operator, value);
        }

        @Override
        public String getValue() {
            return value.toString().toLowerCase();
        }
    }

    private static class StringExpression extends SimpleExpression<String> {

        public StringExpression(String field, String operator, String value) {
            super(field, operator, value);
        }

        @Override
        public String getValue() {
            if (value == null)
                return "NULL";

            return "'" + value + "'";
        }
    }

    private static class PathExpression extends SimpleExpression<String> {

        public PathExpression(String field, String operator, String value) {
            super(field, translateOperator(operator), value);
        }

        @Override
        public String getValue() {
            if (value == null)
                return "NULL";

            return "'" + value + "'";
        }
        private static String translateOperator(String operator) {
            if (operator.equals(RelationalOperator.EQ.op())) return "=";
            if (operator.equals(RelationalOperator.NEQ.op())) return "!=";
            if (operator.equals(RelationalOperator.GR.op())) return "@>";
            if (operator.equals(RelationalOperator.GEQ.op())) throw new IllegalArgumentException();
            if (operator.equals(RelationalOperator.LE.op())) return "<@";
            if (operator.equals(RelationalOperator.LEQ.op())) throw new IllegalArgumentException();

            throw new IllegalArgumentException(operator + " is not a valid path operator");
        }
    }

    private static class FunctionCall {
        private final String name;
        private final List<Object> parameters;

        public FunctionCall(String name, List<Object> parameters) {
            this.name = name;
            this.parameters = parameters;
        }

        public String get() {
            String f =  name + "(";
            final List<Class> expected = FUNC_PARAM_DEF.get(name);
            if (expected == null) throw new IllegalArgumentException("No such function:"+name);
            if (expected.size() != parameters.size())
                throw new IllegalArgumentException(
                        String.format(
                                "Incorrect number of parameters specified to function %s. Expected %s, got %s",
                                name, expected.size(), parameters.size()
                        )
                );

            for (int i = 0; i < parameters.size(); i++) {
                if (parameters.get(i).getClass() != expected.get(i))
                    throw new IllegalArgumentException(
                            String.format(
                                    "Parameter %s is of type %s, but was expecting %s.",
                                    ""+i, parameters.get(i).getClass().getSimpleName(), expected.get(i).getSimpleName()
                            )
                    );
            }
            return f + ")";
        }
    }

    private static final Map<String,List<Class>> FUNC_PARAM_DEF = new HashMap<>();
    static {
        FUNC_PARAM_DEF.put("lower", Arrays.asList(String.class));
        FUNC_PARAM_DEF.put("upper", Arrays.asList(String.class));
        FUNC_PARAM_DEF.put("len", Arrays.asList(String.class));
    }
}
