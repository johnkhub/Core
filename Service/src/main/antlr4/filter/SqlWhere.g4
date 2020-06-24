grammar SqlWhere;

@header {
    import org.joda.time.DateTime;
    import org.joda.time.format.DateTimeFormatter;
    import java.math.BigDecimal;

    import filter.FilterBuilder;
    import filter.FilterBuilder.Field;
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
    :(LBRACKET
                                                                    {
                                                                        query.openScope();
                                                                    }
    )*
    criteria=not_expression[query]                                  {
                                                                        query.expression($criteria.value);
                                                                    }
    (RBRACKET                                                       {
                                                                      query.closeScope();
                                                                    }
    )*
    ( exp_op=expression_operator criteriaN=not_expression[query]    {
                                                                        switch($exp_op.value) {
                                                                            case AND: query.andOp($criteriaN.value); break;
                                                                            case OR: query.orOp($criteriaN.value); break;
                                                                        }
                                                                    }
    )*
    (RBRACKET                                                       {
                                                                        query.closeScope();
                                                                    }
    )*
;

not_expression [FilterBuilder query] returns [Expression value]
    :n=NOT? a=expression                                            {
                                                                        Expression c = $a.value;
                                                                        if ($n != null) c.not();
                                                                        $value = $a.value;
                                                                    }
;

expression_operator returns[GroupOp value]
    : AND                                                           { $value = GroupOp.AND; }
    | OR                                                            { $value = GroupOp.OR; }
;

expression returns[Expression value]
    : a=field b=relational_operator c=element                       {
                                                                        $value = ExpressionFactory.of($a.value, $b.value, $c.value, $c.type);
                                                                    }
;

element returns [Object value, Class type]
    : NULL                                                          { $value = null;  $type = null; }
    | TRUE                                                          { $value = Boolean.TRUE; $type = Boolean.class; }
    | FALSE                                                         { $value = Boolean.FALSE; $type = Boolean.class;}
    | i=WHOLE_NUMBER                                                { $value = Long.parseLong($i.text); $type = Long.class; }
    | f=FLOAT_NUMBER                                                { $value = new BigDecimal($f.text); $type = BigDecimal.class; }
    | s=text                                                        { $value = $s.value; $type = String.class; }
    | fi=field                                                      { $value = $fi.value; $type = Field.class; }
 ;

field returns [String value]
    : fid=FIELD_IDENTIFIER                                          {
                                                                        $value = $fid.text;
                                                                    }
;

text returns [String value]
    : DSQUOTE                                                       { $value = ""; }
    | s=SQUOTED_STRING                                              {
                                                                        String val = $s.text;
                                                                        $value = val == null || val.isEmpty() ? "" : val.substring(1, val.length()-1);
                                                                    }
;

relational_operator returns [RelationalOperator value]
    : EQ                                                            { $value = RelationalOperator.EQ; }
    | NEQ                                                           { $value = RelationalOperator.NEQ; }
    | GRT                                                           { $value = RelationalOperator.GR; }
    | GEQ                                                           { $value = RelationalOperator.EQ; }
    | LST                                                           { $value = RelationalOperator.LE; }
    | LEQ                                                           { $value = RelationalOperator.LEQ; }
 ;

EQ                  : '=';
NEQ                 : '<' '>' | '!' '=';
GRT                 : '>';
GEQ                 : '>' '=' | '!' '<';
LST                 : '<';
LEQ                 : '<' '=' | '!' '>';

AND                 : A N D;
OR                  : O R;
IS                  : I S;
NOT                 : N O T;
LIKE                : L I K E;
NULL                : N U L L;
IN                  : I N;
LOWER               : L O W E R;

TRUE                : T R U E;
FALSE               : F A L S E;

COMMA               : ',';
LBRACKET            : '(';
RBRACKET            : ')';
SQUOTE              : '\'';
DQUOTE              : '"';
DDQUOTE             : DQUOTE DQUOTE;
DSQUOTE             : SQUOTE SQUOTE;

WHOLE_NUMBER        : [+-]?('0'..'9')+;
FLOAT_NUMBER        : [+-]?('0'..'9')+ '.' ('0'..'9')+;
FIELD_IDENTIFIER    : [a-zA-Z][a-zA-Z0-9]*;
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