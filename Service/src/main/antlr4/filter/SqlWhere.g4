grammar SqlWhere;

@header {
    import org.joda.time.DateTime;
    import org.joda.time.format.DateTimeFormatter;
    import java.math.BigDecimal;
    import java.util.Set;

    import filter.FilterBuilder;
    import filter.FilterBuilder.Field;
    import filter.FilterBuilder.Path;
    import filter.FilterBuilder.GroupOp;
    import filter.Expression;
    import filter.ExpressionFactory;
    import filter.ExpressionFactory.RelationalOperator;
}

parse returns[FilterBuilder value]
    : a=parse_inner[new FilterBuilder()] EOF                        { $value = $a.value; }
    | EOF
;

parse_inner [FilterBuilder query] returns [FilterBuilder value]
    @after                                                          { $value = query; }
    :
     x=LBRACKET?                                                    { if ($x != null) query.openScope(); }
     criteria=not_expression[query]
     ( exp_op=expression_operator[query] criteriaN=not_expression[query] )*
     y=RBRACKET?                                                    { if ($y != null) query.closeScope(); }
;

not_expression [FilterBuilder query]
    :
     x=LBRACKET?                                                    { if ($x != null) query.openScope(); }
     n=NOT? a=expression[query]                                     { if ($n != null) $a.value.not(); }
     y=RBRACKET?                                                    { if ($y != null) query.closeScope(); }
;

expression_operator [FilterBuilder query]
    : AND                                                           { query.andOp(); }
    | OR                                                            { query.orOp(); }
;

expression [FilterBuilder query] returns [Expression value]
    :
    x=LBRACKET?                                                     {
                                                                        if ($x != null) query.openScope();
                                                                    }
    (
        TAGS c=element[query]                                       {
                                                                        final Expression e = ExpressionFactory.of(null, null, $c.value, $c.type);
                                                                        query.expression(e);
                                                                        $value = e;

                                                                    }
        |a=field[query] b=relational_operator c=element[query]      {
                                                                        final Expression e = ExpressionFactory.of($a.value, $b.value, $c.value, $c.type);
                                                                        query.expression(e);
                                                                        $value = e;
                                                                    }
    )
    y=RBRACKET?                                                     {
                                                                         if ($y != null) query.closeScope();
                                                                    }
;

element [FilterBuilder query] returns [Object value, Class type]
    : NULL                                                          { $value = null;  $type = null; }
    | TRUE                                                          { $value = Boolean.TRUE; $type = Boolean.class; }
    | FALSE                                                         { $value = Boolean.FALSE; $type = Boolean.class;}
    | i=WHOLE_NUMBER                                                { $value = Long.parseLong($i.text); $type = Long.class; }
    | f=FLOAT_NUMBER                                                { $value = new BigDecimal($f.text); $type = BigDecimal.class; }
    | s=text                                                        { $value = $s.value; $type = String.class; }
    | fi=field[query]                                               { $value = $fi.value; $type = Field.class; }
    | '@' LBRACKET p=text RBRACKET                                  { $value = $p.value; $type = Path.class; }
    | t=set                                                         { $value = $t.value; $type = Set.class; }
 ;

field [FilterBuilder query] returns [String value]
    : fid=FIELD_IDENTIFIER                                          { $value = $fid.text; query.field($fid.text); }
    | LOWER LBRACKET fid=FIELD_IDENTIFIER RBRACKET                  { $value = "LOWER(" + $fid.text + ")"; query.field($fid.text);}
;

text returns [String value]
    : DSQUOTE                                                       { $value = ""; }
    | s=SQUOTED_STRING                                              {
                                                                        String val = $s.text;
                                                                        $value = val == null || val.isEmpty() ? "" : val.substring(1, val.length()-1);
                                                                    }
;

set returns [List<String> value]
    @init                                                           {
                                                                        List<String> set = new ArrayList<>(5);
                                                                        $value = set;
                                                                    }
    : LSQRBRACKET
        s=text                                                      { $value.add($s.text);}
        (
            COMMA s=text                                            { $value.add($s.text);}
        )*
      RSQRBRACKET
;

relational_operator returns [RelationalOperator value]
    : EQ                                                            { $value = RelationalOperator.EQ; }
    | NEQ                                                           { $value = RelationalOperator.NEQ; }
    | GRT                                                           { $value = RelationalOperator.GR; }
    | GEQ                                                           { $value = RelationalOperator.EQ; }
    | LST                                                           { $value = RelationalOperator.LE; }
    | LEQ                                                           { $value = RelationalOperator.LEQ; }
    | LIKE                                                          { $value = RelationalOperator.LIKE; }
 ;

EQ                  : '=';
NEQ                 : '<' '>' | '!' '=';
GRT                 : '>';
GEQ                 : '>' '=';
LST                 : '<';
LEQ                 : '<' '=';
LIKE                : L I K E;

AND                 : A N D;
OR                  : O R;
NOT                 : N O T;

// Builtins
LOWER               : L O W E R;

// Pseudo columns
TAGS                : T A G S;

NULL                : N U L L;
TRUE                : T R U E;
FALSE               : F A L S E;

COMMA               : ',';
LBRACKET            : '(';
RBRACKET            : ')';
LSQRBRACKET         : '[';
RSQRBRACKET         : ']';

SQUOTE              : '\'';
DQUOTE              : '"';
DDQUOTE             : DQUOTE DQUOTE;
DSQUOTE             : SQUOTE SQUOTE;

WHOLE_NUMBER        : [+-]?('0'..'9')+;
FLOAT_NUMBER        : [+-]?('0'..'9')+ '.' ('0'..'9')+;
FIELD_IDENTIFIER    : [a-zA-Z_][a-zA-Z_0-9]*;
DQUOTED_STRING      : DQUOTE (~('"' | '\\' | '\r' | '\n') | '\\' ('"' | '\\'))* DQUOTE;
SQUOTED_STRING      : SQUOTE (~('\'' | '\\' | '\r' | '\n') | '\\' ('\'' | '\\'))* SQUOTE;

WS                  : (' ' | '\t' | '\r' | '\n') -> skip;

fragment A          : ('a'|'A');
fragment B          : ('b'|'B');
fragment C          : ('c'|'C');
fragment D          : ('d'|'D');
fragment E          : ('e'|'E');
fragment F          : ('f'|'F');
fragment G          : ('g'|'G');
fragment H          : ('h'|'H');
fragment I          : ('i'|'I');
fragment J          : ('j'|'J');
fragment K          : ('k'|'K');
fragment L          : ('l'|'L');
fragment M          : ('m'|'M');
fragment N          : ('n'|'N');
fragment O          : ('o'|'O');
fragment P          : ('p'|'P');
fragment Q          : ('q'|'Q');
fragment R          : ('r'|'R');
fragment S          : ('s'|'S');
fragment T          : ('t'|'T');
fragment U          : ('u'|'U');
fragment V          : ('v'|'V');
fragment W          : ('w'|'W');
fragment X          : ('x'|'X');
fragment Y          : ('y'|'Y');
fragment Z          : ('z'|'Z');