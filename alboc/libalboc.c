#include <stdio.h>

int getint (void)
{
  int x;
  scanf("%d", &x);
  return x;
}

int putint (int x)
{
  printf("%d", x);
  return x;
}
