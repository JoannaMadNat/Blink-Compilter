.data
.comm _stack, 800, 8
_top_of_stack: .quad 10 # cushion space for any Whiles or Ifs that don't executed

.text
_myPush:
    movq    _top_of_stack(%rip), %rcx
    leaq    _stack(%rip), %rax
    movq    %r11, (%rax, %rcx, 8)
    incq    _top_of_stack(%rip)
    ret

_myPop:
    decq    _top_of_stack(%rip)
    movq    _top_of_stack(%rip), %rcx
    leaq    _stack(%rip), %rax
    movq    (%rax, %rcx, 8), %rax
    ret
# VVVV the rest is code generated... so surreal man


# 1:visitMethod: start():=letprompt:=printstring("Enter a string of characters:")lets:=readstring()letlen:=getlength(s)letfirstCh:=getchar(s,0)letout1:=printstring("s has "&len&" characters.\n")letout2:=printstring("charAt(0) = '"&firstCh&"'\n")letcomp1:=ifs>"jo"thenprintstring("s > ' '\n")elseprintstring("! s > ' '\n")endletcomp2:=ifs>="puppies"thenprintstring("s >= ' '\n")elseprintstring("! s >= ' '\n")endprintstring("wowsers!")
.global my_start
my_start:
	pushq %rbp
	movq %rsp, %rbp
	subq $64, %rsp
	pushq %rdi
	pushq %rsi
	pushq %rdx
	pushq %rcx
	pushq %r8
	pushq %r9
# 2:visitVariable: prompt
# 2:visitFunction: printstring("Enter a string of characters:")
# 2:visitString: "Enter a string of characters:"
.data
_str0:
.string "Enter a string of characters:"
.text
	leaq _str0(%rip), %r11
	call _myPush
	call _myPop
	movq %rax, %rdi
	call printstring
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, -8(%rbp)
# 3:visitVariable: s
# 3:visitFunction: readstring()
	call readstring
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, -16(%rbp)
# 4:visitVariable: len
# 4:visitFunction: getlength(s)
# 4:visitID: s
	movq -16(%rbp), %r11
	call _myPush
	call _myPop
	movq %rax, %rdi
	call getlength
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, -24(%rbp)
# 6:visitVariable: firstCh
# 6:visitFunction: getchar(s,0)
# 6:visitID: s
	movq -16(%rbp), %r11
	call _myPush
# 6:visitInt: 0
	movq $0, %r11
	call _myPush
	call _myPop
	movq %rax, %rsi
	call _myPop
	movq %rax, %rdi
	call getchar
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, -32(%rbp)
# 8:visitVariable: out1
# 8:visitFunction: printstring("s has "&len&" characters.\n")
# 8:visitConcat: "s has "&len&" characters.\n"
# 8:visitConcat: "s has "&len
# 8:visitString: "s has "
.data
_str1:
.string "s has "
.text
	leaq _str1(%rip), %r11
	call _myPush
# 8:visitID: len
	movq -24(%rbp), %r11
	call _myPush
	call _myPop
	movq %rax, %rdi
	call itos
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, %rsi
	call _myPop
	movq %rax, %rdi
	call concatStr
	movq %rax, %r11
	call _myPush
# 8:visitString: " characters.\n"
.data
_str2:
.string " characters.\n"
.text
	leaq _str2(%rip), %r11
	call _myPush
	call _myPop
	movq %rax, %rsi
	call _myPop
	movq %rax, %rdi
	call concatStr
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, %rdi
	call printstring
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, -40(%rbp)
# 9:visitVariable: out2
# 9:visitFunction: printstring("charAt(0) = '"&firstCh&"'\n")
# 9:visitConcat: "charAt(0) = '"&firstCh&"'\n"
# 9:visitConcat: "charAt(0) = '"&firstCh
# 9:visitString: "charAt(0) = '"
.data
_str3:
.string "charAt(0) = '"
.text
	leaq _str3(%rip), %r11
	call _myPush
# 9:visitID: firstCh
	movq -32(%rbp), %r11
	call _myPush
	call _myPop
	movq %rax, %rdi
	call itos
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, %rsi
	call _myPop
	movq %rax, %rdi
	call concatStr
	movq %rax, %r11
	call _myPush
# 9:visitString: "'\n"
.data
_str4:
.string "'\n"
.text
	leaq _str4(%rip), %r11
	call _myPush
	call _myPop
	movq %rax, %rsi
	call _myPop
	movq %rax, %rdi
	call concatStr
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, %rdi
	call printstring
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, -48(%rbp)
# 11:visitVariable: comp1
# 11:visit_IF: ifs>"jo"thenprintstring("s > ' '\n")elseprintstring("! s > ' '\n")end
# 11:visitGreater: s>"jo"
# 11:visitID: s
	movq -16(%rbp), %r11
	call _myPush
# 11:visitString: "jo"
.data
_str5:
.string "jo"
.text
	leaq _str5(%rip), %r11
	call _myPush
	call _myPop
	movq %rax, %rsi
	call _myPop
	movq %rax, %rdi
	call compString
	movq %rax, %r11
	call _myPush
	movq $0, %r11
	call _myPush
	call _myPop
	movq %rax, %rbx
	call _myPop
	cmpq %rbx, %rax
	jg _doif1
	jmp _else1
_doif1:
	movq $1, %r11
	call _myPush
	jmp _endif1
_else1:
	movq $0, %r11
	call _myPush
_endif1:
	call _myPop
	cmpq $0, %rax
	jne _doif2
	jmp _else2
_doif2:
# 12:visitFunction: printstring("s > ' '\n")
# 12:visitString: "s > ' '\n"
.data
_str6:
.string "s > ' '\n"
.text
	leaq _str6(%rip), %r11
	call _myPush
	call _myPop
	movq %rax, %rdi
	call printstring
	movq %rax, %r11
	call _myPush
	jmp _endif2
_else2:
# 14:visitFunction: printstring("! s > ' '\n")
# 14:visitString: "! s > ' '\n"
.data
_str7:
.string "! s > ' '\n"
.text
	leaq _str7(%rip), %r11
	call _myPush
	call _myPop
	movq %rax, %rdi
	call printstring
	movq %rax, %r11
	call _myPush
_endif2:
	call _myPop
	movq %rax, -56(%rbp)
# 17:visitVariable: comp2
# 17:visit_IF: ifs>="puppies"thenprintstring("s >= ' '\n")elseprintstring("! s >= ' '\n")end
# 17:visitGreater_Equal: s>="puppies"
# 17:visitID: s
	movq -16(%rbp), %r11
	call _myPush
# 17:visitString: "puppies"
.data
_str8:
.string "puppies"
.text
	leaq _str8(%rip), %r11
	call _myPush
	call _myPop
	movq %rax, %rsi
	call _myPop
	movq %rax, %rdi
	call compString
	movq %rax, %r11
	call _myPush
	movq $0, %r11
	call _myPush
	call _myPop
	movq %rax, %rbx
	call _myPop
	cmpq %rbx, %rax
	jge _doif3
	jmp _else3
_doif3:
	movq $1, %r11
	call _myPush
	jmp _endif3
_else3:
	movq $0, %r11
	call _myPush
_endif3:
	call _myPop
	cmpq $0, %rax
	jne _doif4
	jmp _else4
_doif4:
# 18:visitFunction: printstring("s >= ' '\n")
# 18:visitString: "s >= ' '\n"
.data
_str9:
.string "s >= ' '\n"
.text
	leaq _str9(%rip), %r11
	call _myPush
	call _myPop
	movq %rax, %rdi
	call printstring
	movq %rax, %r11
	call _myPush
	jmp _endif4
_else4:
# 20:visitFunction: printstring("! s >= ' '\n")
# 20:visitString: "! s >= ' '\n"
.data
_str10:
.string "! s >= ' '\n"
.text
	leaq _str10(%rip), %r11
	call _myPush
	call _myPop
	movq %rax, %rdi
	call printstring
	movq %rax, %r11
	call _myPush
_endif4:
	call _myPop
	movq %rax, -64(%rbp)
# 23:visitFunction: printstring("wowsers!")
# 23:visitString: "wowsers!"
.data
_str11:
.string "wowsers!"
.text
	leaq _str11(%rip), %r11
	call _myPush
	call _myPop
	movq %rax, %rdi
	call printstring
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rbp, %rsp
	popq %rbp
	ret
.text
.global main
main:
	pushq %rbp
	movq %rsp, %rbp
	call my_start
	movq %rbp, %rsp
	popq %rbp
	ret

