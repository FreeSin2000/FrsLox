#ifndef clox_chunk_h
#define clox_chunk_h

#include "common.h"
#include "value.h"

typedef enum
{
    OP_CONSTANT,
    OP_RETURN,
} OpCode;

typedef struct
{
    int count;
    int capacity;
    int *linesNum;
    int *linesOffset;
} Lines;

typedef struct
{
    int count;
    int capacity;
    uint8_t *code;
    ValueArray constants;
    Lines lines;
} Chunk;

void initChunk(Chunk *chunk);
void freeChunk(Chunk *chunk);
void initLines(Lines *lines);
void freeLines(Lines *lines);
int getLine(Chunk *chunk, int offset);
void writeChunk(Chunk *chunk, uint8_t byte, int line);
void writeLines(Chunk *chunk, int line);
int addConstant(Chunk *chunk, Value value);

#endif