CLOX_PATH = .
ALL_SRC = $(wildcard $(CLOX_PATH)/*.c)
ALL_HEADER = $(wildcard $(CLOX_PATH)/*.h)
clox: $(ALL_SRC) $(ALL_HEADER)
	gcc $(ALL_HEADER) $(ALL_SRC) -o clox -g -Wall
