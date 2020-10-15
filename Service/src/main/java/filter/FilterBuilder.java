package filter;

import za.co.imqs.coreservice.dataaccess.exception.ValidationFailureException;

import java.util.*;

public class FilterBuilder {
    public enum GroupOp implements Node {
        AND, OR
    }

    public interface Field {}
    public interface Path {}

    private Long limit;
    private Long offset;
    private final List<String> orderBy = new ArrayList<>();
    private final List<String> groupBy = new ArrayList<>();
    private boolean descending = false;

    private final Scope scope = new Scope();
    private int bracketCount = 0;

    private final Set<String> fieldReferences = new HashSet<>();


    public FilterBuilder() {
        fieldReferences.add("asset_id");
    }

    public FilterBuilder openScope() {
        scope.add(new OpenBracket());
        bracketCount++;
        return this;
    }

    public FilterBuilder closeScope() {
        scope.add(new CloseBracket());
        bracketCount--;
        return this;
    }

    public FilterBuilder andOp() {
        scope.add(GroupOp.AND);
        return this;
    }

    public FilterBuilder orOp() {
        scope.add(GroupOp.OR);
        return this;
    }

    public FilterBuilder expression(Expression e) {
        scope.add(e);
        return this;
    }

    public FilterBuilder field(String field) {
        fieldReferences.add(field);
        return this;
    }

    // Having the list of fields that are reference in the filter makes it possible
    // to join any additional tables that are required to apply the filter, without
    // exposing the internal complexities of the schema to the outside.
    public Set<String> getFields() {
        return fieldReferences;
    }

    public FilterBuilder orderBy(String ...fields) {
        return orderBy(Arrays.asList(fields));
    }

    public FilterBuilder orderBy(boolean descending, String ...fields) {
        return orderBy(descending, Arrays.asList(fields));
    }

    public FilterBuilder orderBy(List<String> fields) {
        return orderBy(false, fields);
    }

    public FilterBuilder orderBy(boolean descending, List<String> fields) {
        orderBy.addAll(fields);
        this.descending = descending;
        return this;
    }

    public FilterBuilder groupBy(String ...fields) {
        return groupBy(Arrays.asList(fields));
    }

    public FilterBuilder groupBy(List<String> fields) {
        groupBy.addAll(fields);
        return this;
    }

    public FilterBuilder limit(long limit) {
        if (limit < 0) throw new IllegalArgumentException();
        this.limit = limit;
        return this;
    }

    public FilterBuilder offset(long offset) {
        if (offset < 0) throw new IllegalArgumentException();
        this.offset = offset;
        return this;
    }

    public String build() {
        if (bracketCount != 0) throw new ValidationFailureException("Unmatched bracket(s) in filter.");
        String filter = "";
        filter = groupBy.isEmpty() ? filter : (filter + " GROUP BY " + String.join(",", groupBy));
        filter = orderBy.isEmpty() ? filter : (filter + " ORDER BY " + String.join(",", orderBy));
        filter = orderBy.isEmpty() ? filter : (filter + (descending ? " DESC" : " ASC"));
        filter = limit == null ? filter : (filter + " LIMIT " + limit.toString());
        filter = offset == null ? filter : (filter + " OFFSET " + offset.toString());

        return scope.get() + filter;
    }

    private static class Scope {
        private final List<Node> nodes = new LinkedList<>();

        public void add(Node n) {
            nodes.add(n);
        }

        public String get() {
            StringBuilder group = new StringBuilder();
            for (Node c : nodes) {
                group.append(c.toString()).append(" ");
            }
            return group.toString();
        }
    }

    private static class OpenBracket implements Node {
        public String toString() {
            return "(";
        }
    }

    private static class CloseBracket implements Node {
        public String toString() {
            return ")";
        }
    }

}
