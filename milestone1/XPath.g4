grammar XPath;

doc
	: DOC '(' filePath ')'
	;

filePath
    : '"' fileName '"'
    ;

fileName
    : STR('.' STR)?
    ;

ap
	: doc '/' rp                  # ApChildren
	| doc '//' rp                 # ApAll
	;

rp
	: STR                          # TagName
	| '*'                          # AllChildren
	| '.'                          # Current
	| '..'                         # Parent
	| TXT                          # Txt
	| '@' STR                      # Attribute
	| '(' rp ')'                   # RpwithP
	| rp '/' rp                    # RpChildren
	| rp '//' rp                   # RpAll
	| rp '[' filter ']'            # RpFilter
	| rp ',' rp                    # TwoRp
	;

filter
	: rp                           # FltRp
	| rp '=' rp                    # FltEqual
	| rp 'eq' rp                   # FltEqual
	| rp '==' rp                   # FltIs
	| rp 'is' rp                   # FltIs
	| '(' filter ')'               # FltwithP
	| filter 'and' filter          # FltAnd
	| filter 'or' filter           # FltOr
	| 'not' filter                 # FltNot
	;

DOC: 'doc' ;
STR: [a-zA-Z0-9_-]+;
TXT: 'text()';
WhiteSpace : [\r\t]+ -> skip;
