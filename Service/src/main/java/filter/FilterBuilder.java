package filter;

import java.util.*;

public class FilterBuilder {
    public enum GroupOp implements Node {
        NOT, AND, OR
    }

    public interface Field {};

    private Long limit;
    private Long offset;
    private final List<String> orderBy = new ArrayList<>();
    private final List<String> groupBy = new ArrayList<>();
    private final List<Scope> scopes = new LinkedList<>();

    private Scope scope = new Scope();

    public FilterBuilder() {
        scopes.add(scope);
    }

    public FilterBuilder openScope() {
        scope = new Scope();
        return this;
    }

    public FilterBuilder closeScope() {
        scopes.add(scope);
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

    public FilterBuilder andOp(Expression e) {
        scope.add(GroupOp.AND);
        scope.add(e);
        return this;
    }

    public FilterBuilder orOp(Expression e) {
        scope.add(GroupOp.OR);
        scope.add(e);
        return this;
    }

    public FilterBuilder expression(Expression e) {
        scope.add(e);
        return this;
    }

    public FilterBuilder orderBy(String ...fields) {
        return orderBy(Arrays.asList(fields));
    }

    public FilterBuilder orderBy(List<String> fields) {
        orderBy.addAll(fields);
        return this;
    }

    public FilterBuilder groupBy(String ...fields) {
        return groupBy(Arrays.asList(fields));
    }

    public FilterBuilder groupBy(List<String> fields) {
        orderBy.addAll(fields);
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
        String gs = new String();

        for (Scope g : scopes) {
            gs += "("+g.get()+")";
        }

        String filter = new String();
        filter = groupBy.isEmpty() ? filter : (filter + " GROUP BY " + String.join(",", groupBy));
        filter = orderBy.isEmpty() ? filter : (filter + " ORDER BY " + String.join(",", orderBy));
        filter = limit == null ? filter : (filter + " LIMIT " + limit.toString());
        filter = offset == null ? filter : (filter + " OFFSET " + limit.toString());

        return gs + filter;
    }

    private class Scope {
        private final List<Node> nodes = new LinkedList<>();

        public void add(Node n) {
            nodes.add(n);
        }

        public String get() {
            String group = "";
            for (Node c : nodes) {
                group += c.toString() ;
            }
            return group;
        }
    }
}
