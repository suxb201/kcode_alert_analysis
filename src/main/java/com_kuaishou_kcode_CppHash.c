#include <jni.h>
#include <stdio.h>
#include "com_kuaishou_kcode_CppHash.h"

int mo =1024*1024-1;
JNIEXPORT jint JNICALL Java_com_kuaishou_kcode_CppHash_get_1hash (JNIEnv * env, jclass qwe, jstring a, jstring b, jstring c, jstring d,jint base_time) {
   int len_a = (*env)->GetStringLength(env, a);
   int len_b = (*env)->GetStringLength(env, b);
   int len_c = (*env)->GetStringLength(env, c);
   const jchar* ch_a = (*env)->GetStringChars(env, a, 0);
   const jchar* ch_b = (*env)->GetStringChars(env, b, 0);
   const jchar* ch_time = (*env)->GetStringChars(env, d, 0);

   int code_time = ch_time[11] * 600 + ch_time[12] * 60 + ch_time[14] * 10 + ch_time[15]-base_time;
   int code_a=0;
   int code_b=0;
   for (int i = 0; i < len_a; i++) code_a = 31 * code_a + ch_a[i];
   for (int i = 0; i < len_b; i++) code_b = 31 * code_b + ch_b[i];
    int code1 = ((code_a<<5)-code_a+code_b)&mo;
    int code2 = (code_time<<1)+(len_c & 1);
//   for(int i=0;i<length;i++) {
//    printf("%c",ch[i]);
//   }
//    printf("\n");
   return (code1<<8)+code2;
};