#include <syscall.h>
#include <string.h>
#define BUFFSZ 2000
#define INTSZ 20

#define INT long long int

INT printint(INT num)
{
  char buf[INTSZ];
  char result[INTSZ] = "0\n";
  char *pos = buf;
  char *writeptr = result;
  long long int numWritten;
  long long int originalNum = num;

  // Handle negative numbers
  if (num < 0)
  {
    *writeptr++ = '-';
    num = -num;
  }

  if (num > 0)
  {
    // Build the number in reverse order
    while (num > 0)
    {
      *pos++ = (num % (INTSZ / 2)) + '0';
      num /= (INTSZ / 2);
    }
    pos--;

    // Now we need to copy the results into the output buffer, reversed
    while (pos > buf)
    {
      *writeptr++ = *pos--;
    }
    *writeptr++ = *pos;
    *writeptr++ = 10;
    *writeptr++ = 0;
  }
  else
  {
    // number is 0; use default result
    writeptr = result + 3;
  }

  write(1, result, (writeptr - result) - 1);
  return originalNum;
}

// The "I'll get a number out of you" readint
// This function will extract a number out of any sequence of characters like
// my dog eating all the treats and leaving out the kibble. picky picky boi
// It uses the strategy "take what you can and run!"
INT readint()
{
  char buf[INTSZ];
  char *traverse = buf;
  INT sign = 1;
  INT number = 0;

  read(1, buf, INTSZ);

  if (buf[0] == '-')
  { //detect negative
    sign = -1;
    traverse++;
  }

  for (int i = 0; i < INTSZ; i++)
  {
    char digit = *(traverse + i);
    if (digit == '\0' || digit < '0' || digit > '9' || digit == '\n')
      break;

    number *= 10;
    number += (digit - '0'); //build the number by multiplying ten every time
  }

  number *= sign;
  return number;
}

INT getlength(char *str)
{
  INT size = 0;
  for (int i = 0; i < BUFFSZ; ++i)
  {
    if (*(str + i) == '\0')
      break;
    size++;
  }

  return size;
}

INT printchar(INT ch)
{
  write(1, &ch, 1);
  return ch;
}

char *printstring(char *str)
{
  INT size = getlength(str);

  for (int i = 0; i < size; ++i)
    printchar(str[i]);
  return str;
}

INT getchar(char *str, INT pos)
{
  INT size = getlength(str);

  for (int i = 0; i < size; ++i)
    if (i == pos)
      return *(str + i);

  return 0;
}

INT readchar()
{
  INT buf;
  read(1, &buf, 1);
  return buf;
}

char *readstring()
{
  int sz = 100;
  char *res = calloc(sz, 1);

  for (int i = 0; i < sz; ++i)
  {
    char buf = readchar();

    if (buf == '\n')
    {
      res[i] = '\0';
      break;
    }

    res[i] = buf;
  }

  return res;
}

INT setchar(char *str, INT pos, INT ch)
{
  int size = getlength(str);

  for (int i = 0; i < size; ++i)
    if (i == pos)
    {
      str[i] = ch;
      break;
    }

  return ch;
}

// Invisible functions VVV (functions that are not used in the Blink standard library)

INT allocateMem(INT size)
{
  INT members = size / 8;
  INT *buf = calloc(members, 8);
  return buf;
}

char *itos(INT num)
{
  char *buf[INTSZ]; // I'm going crazy with this calloc stuff
  char result[INTSZ] = "0";
  char *pos = buf;
  char *writeptr = result;
  long long int numWritten;
  long long int originalNum = num;

  // Handle negative numbers
  if (num < 0)
  {
    *writeptr++ = '-';
    num = -num;
  }

  if (num > 0)
  {
    // Build the number in reverse order
    while (num > 0)
    {
      *pos++ = (num % (INTSZ / 2)) + '0';
      num /= (INTSZ / 2);
    }
    pos--;

    // Now we need to copy the results into the output buffer, reversed
    while (pos > buf)
    {
      *writeptr++ = *pos--;
    }
    *writeptr++ = *pos;
    *writeptr++ = '\0';
  }
  else
  {
    // number is 0; use default result
    writeptr = result + 3;
  }

  int len = getlength(result);
  char *res = calloc(len, 1);
  for (int i = 0; i < len; ++i)
    res[i] = result[i];
  res[len] = '\0';

  return res;
}

char *concatStr(char *dest, char *src)
{
  int destSZ = getlength(dest), srcSZ = getlength(src);

  char *buf = calloc(destSZ + srcSZ, 1);

  for (int i = 0; i < destSZ; ++i)
    buf[i] = dest[i];
  buf[destSZ] = '\0';

  strncat(buf, src, destSZ + srcSZ);
  return buf;
}

INT nullCheck(INT address)
{
  if (address != 0)
    return address;

  printstring("Hi there. It looks like you tried to dereference a null point.\n"
              "It's alright, it's not like there was anything important in memory anyway.\n"
              "These vikings are here to help you find it while also finding their way to\n"
              "a mysterious island called Garðarsholmur. Hope ya'll find what you're looking for...\n\n"
              "                        A_______\n"
              "                        |______<\n"
              "                        ||\n"
              "                  ______||_______\n"
              "                  \\##############\\\n"
              "                   \\##############\\\n"
              "                   |               |\n"
              "                   |               |\n"
              "                   |###############|\n"
              "                   |###############|\n"
              "                   |               |\n"
              "                   |               |\n"
              "                   |###############|\n"
              "                  /###############/           @@\n"
              "          /\\     /_______________/           (  C\n"
              " (@\\     ( \")   /\\\\  /\\\\||/\\\\  /\\\\  /\\\\     / /'\n"
              "    \\_   (\\_)  ( \"))( \")|( \"))( \"))( \"))   / /\n"
              "     \\```--/----/(o)-/(o)-/(o)-/(o)-/(o)--' /\n"
              "~~~~~~~~~/ ~~~/~~~~/~~~~/~~~~/~~~~/~~~~~~~~~~~~~~~~~\n"
              "VK  - - '  ( ' )( ' )( ' )( ' )( ' )\n"
              "\n");
  exit(1);
}

INT compString(char *src, char *src2)
{
  int len1 = getlength(src);
  int len2 = getlength(src2);

  int buf = len1;
  if(len1 > len2)
    buf = len2;

  if(len1 != len2)
    buf++;

  INT sz = strncmp(src, src2, buf);
  return sz;
}

int checkAssign(INT *VFTDesired, INT *VFTGiven)
{
  if(VFTGiven == 0)
    return VFTGiven;

  while (VFTGiven != 0)
  {
    if (VFTGiven == VFTDesired)
      return VFTGiven;
    VFTGiven = *VFTGiven;
  }

  printstring("Invalid assignment. YOU DINGUS!!!\n");
  exit(1);
}
