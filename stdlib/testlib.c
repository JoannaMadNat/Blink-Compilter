#include <stdio.h>

int main() {
    char str[20] = "jojo";
    char str2[20] = "jo";    

 int len1 = getlength(str);
  int len2 = getlength(str2);

  int buf = len1;
  if(len1 > len2)
    buf = len2;
    printint(buf);

//  printf("%d\n", compString(str, str2));// => this produces <
//     printf("%d\n", compString(str2, str));// => this produces equal

  printf("%d\n", compString("jo", "Jojo"));
  printf("%d\n", compString("jo", "jo"));

    return 0;
}

