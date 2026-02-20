JLOX_PATH = com/craftinginterpreters/lox
TOOL_PATH = com/craftinginterpreters/tool
ALL_SRC = $(wildcard $(JLOX_PATH)/*.java)
JLOX_BUILD_DIR = ./build

PRINTER_SRC = $(JLOX_PATH)/AstPrinter.java \
			  $(JLOX_PATH)/RPNPrinter.java

JLOX_SRC = $(filter-out $(PRINTER_SRC), $(ALL_SRC))

jlox:	 generate_ast $(JLOX_SRC)
	javac -d $(JLOX_BUILD_DIR) $(JLOX_SRC)

ast_printer:	jlox
	java -cp $(JLOX_BUILD_DIR) $(JLOX_PATH)/AstPrinter

rpn_printer:	jlox
	java -cp $(JLOX_BUILD_DIR) $(JLOX_PATH)/RPNPrinter

all:	jlox

clean:
	rm $(JLOX_BUILD_DIR)/*.class

.PHONY: run run_file generate_ast

run:	jlox
	java -cp $(JLOX_BUILD_DIR) $(JLOX_PATH)/Lox

run_file:	jlox
	java -cp $(JLOX_BUILD_DIR) $(JLOX_PATH)/Lox ./test.jlox

generate_ast:	${TOOL_PATH}/GenerateAst.java
# 	javac ${TOOL_PATH}/GenerateAst.java
	java ${TOOL_PATH}/GenerateAst.java ${JLOX_PATH}