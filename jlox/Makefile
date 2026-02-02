JLOX_PATH = com/craftinginterpreters/lox

jlox:	${JLOX_PATH}/Lox.java ${JLOX_PATH}/TokenType.java ${JLOX_PATH}/Token.java ${JLOX_PATH}/Scanner.java
	javac -d classes ${JLOX_PATH}/*.java

run:	jlox
	java ${JLOX_PATH}/Lox