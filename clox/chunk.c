#include <stdlib.h>

#include "chunk.h"
#include "memory.h"

void initChunk(Chunk *chunk)
{
    chunk->count = 0;
    chunk->capacity = 0;
    chunk->code = NULL;

    initLines(&chunk->lines);
    initValueArray(&chunk->constants);
}

void initLines(Lines *lines)
{
    lines->count = 0;
    lines->capacity = 0;
    lines->linesNum = NULL;
    lines->linesOffset = NULL;
}

void writeChunk(Chunk *chunk, uint8_t byte, int line)
{
    if (chunk->capacity < chunk->count + 1)
    {
        int oldCapacity = chunk->capacity;
        chunk->capacity = GROW_CAPACITY(oldCapacity);
        chunk->code = GROW_ARRAY(uint8_t, chunk->code,
                                 oldCapacity, chunk->capacity);
    }

    chunk->code[chunk->count] = byte;
    writeLines(chunk, line);
    chunk->count++;
}

void writeLines(Chunk *chunk, int line)
{
    Lines *lines = &chunk->lines;
    int curLine = lines->count;
    if (curLine == 0 || lines->linesNum[curLine - 1] != line)
    {
        if (lines->capacity < lines->count + 1)
        {
            int oldCapacity = lines->capacity;
            lines->capacity = GROW_CAPACITY(oldCapacity);
            lines->linesNum = GROW_ARRAY(int, lines->linesNum,
                                         oldCapacity, lines->capacity);
            lines->linesOffset = GROW_ARRAY(int, lines->linesOffset,
                                            oldCapacity, lines->capacity);
        }
        lines->count++;
        curLine = lines->count;
        lines->linesNum[curLine - 1] = line;
        lines->linesOffset[curLine - 1] = 0;
    }
    lines->linesOffset[curLine - 1]++;
}

int addConstant(Chunk *chunk, Value value)
{
    writeValueArray(&chunk->constants, value);
    return chunk->constants.count - 1;
}

void freeChunk(Chunk *chunk)
{
    FREE_ARRAY(uint8_t, chunk->code, chunk->capacity);
    freeLines(&chunk->lines);
    freeValueArray(&chunk->constants);
    initChunk(chunk);
}

void freeLines(Lines *lines)
{
    FREE_ARRAY(int, lines->linesNum, lines->capacity);
    FREE_ARRAY(int, lines->linesOffset, lines->capacity);
    initLines(lines);
}

int getLine(Chunk *chunk, int offset)
{
    Lines *lines = &chunk->lines;
    for (int i = 0; i < lines->count; i++)
    {
        if (offset < lines->linesOffset[i])
            return lines->linesNum[i];
        offset -= lines->linesOffset[i];
    }
    return -1;
}