#include <stdio.h>
#include <stdlib.h>
#include <memory.h>
#include <assert.h>

typedef struct ListNode* ListNodePtr;
typedef struct ListNode {
    ListNodePtr next, prev;
    char *val;
} ListNode;

ListNode * insert(ListNode * l, const char * val) {
    assert(l);
    ListNode *cur_node = (ListNodePtr)malloc(sizeof(ListNode));
    cur_node->val = (char *)malloc(strlen(val) + 1);
    assert(cur_node);
    strcpy(cur_node->val, val);
    if(l->next == NULL) {
        l->next = cur_node;
        l->prev = cur_node;
        cur_node->prev = l;
        cur_node->next = l;
    } else {
        cur_node->prev = l->prev;
        cur_node->next = l->next;
        l->next->prev = cur_node;
        l->next = cur_node;
    }
    return l;
}

ListNode *new_node(const char * val) {
    ListNode * cur_node = (ListNodePtr)malloc(sizeof(ListNode));
    cur_node->prev = NULL;
    cur_node->next = NULL;
    cur_node->val = (char *)malloc(strlen(val) + 1);
    strcpy(cur_node->val, val);
    return cur_node;
}

int main() {
    printf("Hello clox!\n");
    return 0;
}