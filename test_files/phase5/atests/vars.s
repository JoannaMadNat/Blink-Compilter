.data
.comm _stack, 800, 8
_top_of_stack: .quad 0

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


# 1:visitVariable: glob
# 3:visitBlink_class: typePoint(_x:int,_y:int):=memberx:int:=23memberchange0:=(x:=x+1)memberaccess0:=printint(x)memberchangeg:=(glob:=glob+1)memberaccessg:=printint(glob)memberpr:=printstring("End of function Point_constructor\n")membergetX(c:int,i:int,j:int,k:int,o:int,p:int,kk:int,ll:int):=letl:=103letchange1:=(l:=l+1)letaccess1:=printint(l)letchange2:=(kk:=kk+1)letaccess2:=printint(kk)letchange3:=(x:=x+1)letaccess3:=printint(x)letchange5:=(glob:=glob+1)letaccess5:=printint(glob)letchange6:=(c:=c+1)letaccess6:=printint(c)printstring("End of function Point_getX\n")end
.data
VFTPoint:
.quad 0
.quad Point_getX
.text
# 14:visitMethod: getX(c:int,i:int,j:int,k:int,o:int,p:int,kk:int,ll:int):=letl:=103letchange1:=(l:=l+1)letaccess1:=printint(l)letchange2:=(kk:=kk+1)letaccess2:=printint(kk)letchange3:=(x:=x+1)letaccess3:=printint(x)letchange5:=(glob:=glob+1)letaccess5:=printint(glob)letchange6:=(c:=c+1)letaccess6:=printint(c)printstring("End of function Point_getX\n")
.global Point_getX
Point_getX:
	pushq %rbp
	movq %rsp, %rbp
	subq $96, %rsp
	pushq %rdi
	pushq %rsi
	pushq %rdx
	pushq %rcx
	pushq %r8
	pushq %r9
# 15:visitVariable: l
# 15:visitInt: 103
	movq $103, %r11
	call _myPush
	call _myPop
	movq %rax, -8(%rbp)
# 17:visitVariable: change1
# 17:Reassign: l:=l+1
# 17:visitAdd: l+1
# 17:visitID: l
	movq -8(%rbp), %r11
	call _myPush
# 17:visitInt: 1
	movq $1, %r11
	call _myPush
	call _myPop
	movq %rax, %rbx
	call _myPop
	addq %rbx, %rax
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, -8(%rbp)
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, -16(%rbp)
# 18:visitVariable: access1
# 18:visitFunction: printint(l)
# 18:visitID: l
	movq -8(%rbp), %r11
	call _myPush
	call _myPop
	movq %rax, %rdi
	call printint
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, -24(%rbp)
# 20:visitVariable: change2
# 20:Reassign: kk:=kk+1
# 20:visitAdd: kk+1
# 20:visitID: kk
	movq 24(%rbp), %r11
	call _myPush
# 20:visitInt: 1
	movq $1, %r11
	call _myPush
	call _myPop
	movq %rax, %rbx
	call _myPop
	addq %rbx, %rax
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, 24(%rbp)
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, -32(%rbp)
# 21:visitVariable: access2
# 21:visitFunction: printint(kk)
# 21:visitID: kk
	movq 24(%rbp), %r11
	call _myPush
	call _myPop
	movq %rax, %rdi
	call printint
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, -40(%rbp)
# 23:visitVariable: change3
# 23:Reassign: x:=x+1
# 23:visitAdd: x+1
# 23:visitID: x
	movq -104(%rbp), %r11
	call _myPush
	call _myPop
	movq %rax, %rdi
	call nullCheck
	movq 40(%rax), %r11
	call _myPush
# 23:visitInt: 1
	movq $1, %r11
	call _myPush
	call _myPop
	movq %rax, %rbx
	call _myPop
	addq %rbx, %rax
	movq %rax, %r11
	call _myPush
	movq -104(%rbp), %r11
	call _myPush
	call _myPop
	movq %rax, %rdi
	call nullCheck
	movq %rax, %r12
	call _myPop
	movq %rax, 40(%r12)
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, -48(%rbp)
# 24:visitVariable: access3
# 24:visitFunction: printint(x)
# 24:visitID: x
	movq -104(%rbp), %r11
	call _myPush
	call _myPop
	movq %rax, %rdi
	call nullCheck
	movq 40(%rax), %r11
	call _myPush
	call _myPop
	movq %rax, %rdi
	call printint
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, -56(%rbp)
# 26:visitVariable: change5
# 26:Reassign: glob:=glob+1
# 26:visitAdd: glob+1
# 26:visitID: glob
	movq _glob(%rip), %r11
	call _myPush
# 26:visitInt: 1
	movq $1, %r11
	call _myPush
	call _myPop
	movq %rax, %rbx
	call _myPop
	addq %rbx, %rax
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, _glob(%rip)
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, -64(%rbp)
# 27:visitVariable: access5
# 27:visitFunction: printint(glob)
# 27:visitID: glob
	movq _glob(%rip), %r11
	call _myPush
	call _myPop
	movq %rax, %rdi
	call printint
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, -72(%rbp)
# 29:visitVariable: change6
# 29:Reassign: c:=c+1
# 29:visitAdd: c+1
# 29:visitID: c
	movq -112(%rbp), %r11
	call _myPush
# 29:visitInt: 1
	movq $1, %r11
	call _myPush
	call _myPop
	movq %rax, %rbx
	call _myPop
	addq %rbx, %rax
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, -112(%rbp)
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, -80(%rbp)
# 30:visitVariable: access6
# 30:visitFunction: printint(c)
# 30:visitID: c
	movq -112(%rbp), %r11
	call _myPush
	call _myPop
	movq %rax, %rdi
	call printint
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, -88(%rbp)
# 32:visitFunction: printstring("End of function Point_getX\n")
# 32:visitString: "End of function Point_getX\n"
.data
_str0:
.string "End of function Point_getX\n"
.text
	leaq _str0(%rip), %r11
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
.global c_Point
c_Point:
	pushq %rbp
	movq %rsp, %rbp
	call _myPop
	pushq %rax
	pushq $0
	movq -8(%rbp), %r13
	call _myPop
	movq %rax, 32(%r13)
	call _myPop
	movq %rax, 24(%r13)
# 4:visitVariable: x
# 4:visitInt: 23
	movq $23, %r11
	call _myPush
	movq -8(%rbp), %rdi
	call nullCheck
	movq %rax, %r12
	call _myPop
	movq %rax, 40(%r12)
# 6:visitVariable: change0
# 6:Reassign: x:=x+1
# 6:visitAdd: x+1
# 6:visitID: x
	movq -8(%rbp), %r11
	call _myPush
	call _myPop
	movq %rax, %rdi
	call nullCheck
	movq 40(%rax), %r11
	call _myPush
# 6:visitInt: 1
	movq $1, %r11
	call _myPush
	call _myPop
	movq %rax, %rbx
	call _myPop
	addq %rbx, %rax
	movq %rax, %r11
	call _myPush
	movq -8(%rbp), %r11
	call _myPush
	call _myPop
	movq %rax, %rdi
	call nullCheck
	movq %rax, %r12
	call _myPop
	movq %rax, 40(%r12)
	movq %rax, %r11
	call _myPush
	movq -8(%rbp), %rdi
	call nullCheck
	movq %rax, %r12
	call _myPop
	movq %rax, 48(%r12)
# 7:visitVariable: access0
# 7:visitFunction: printint(x)
# 7:visitID: x
	movq -8(%rbp), %r11
	call _myPush
	call _myPop
	movq %rax, %rdi
	call nullCheck
	movq 40(%rax), %r11
	call _myPush
	call _myPop
	movq %rax, %rdi
	call printint
	movq %rax, %r11
	call _myPush
	movq -8(%rbp), %rdi
	call nullCheck
	movq %rax, %r12
	call _myPop
	movq %rax, 56(%r12)
# 9:visitVariable: changeg
# 9:Reassign: glob:=glob+1
# 9:visitAdd: glob+1
# 9:visitID: glob
	movq _glob(%rip), %r11
	call _myPush
# 9:visitInt: 1
	movq $1, %r11
	call _myPush
	call _myPop
	movq %rax, %rbx
	call _myPop
	addq %rbx, %rax
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, _glob(%rip)
	movq %rax, %r11
	call _myPush
	movq -8(%rbp), %rdi
	call nullCheck
	movq %rax, %r12
	call _myPop
	movq %rax, 64(%r12)
# 10:visitVariable: accessg
# 10:visitFunction: printint(glob)
# 10:visitID: glob
	movq _glob(%rip), %r11
	call _myPush
	call _myPop
	movq %rax, %rdi
	call printint
	movq %rax, %r11
	call _myPush
	movq -8(%rbp), %rdi
	call nullCheck
	movq %rax, %r12
	call _myPop
	movq %rax, 72(%r12)
# 12:visitVariable: pr
# 12:visitFunction: printstring("End of function Point_constructor\n")
# 12:visitString: "End of function Point_constructor\n"
.data
_str1:
.string "End of function Point_constructor\n"
.text
	leaq _str1(%rip), %r11
	call _myPush
	call _myPop
	movq %rax, %rdi
	call printstring
	movq %rax, %r11
	call _myPush
	movq -8(%rbp), %rdi
	call nullCheck
	movq %rax, %r12
	call _myPop
	movq %rax, 80(%r12)
	movq -8(%rbp), %rax
	movq %rbp, %rsp
	popq %rbp
	ret
# 35:visitMethod: getX(c:int,i:int,j:int,k:int,o:int,p:int,kk:int):=letl:=103letchange1:=(l:=l+1)letaccess1:=printint(l)letchange2:=(kk:=kk+1)letaccess2:=printint(kk)letchange5:=(glob:=glob+1)letaccess5:=printint(glob)letchange6:=(c:=c+1)letaccess6:=printint(c)printstring("End of function getX\n")
.global my_getX
my_getX:
	pushq %rbp
	movq %rsp, %rbp
	subq $80, %rsp
	pushq %rdi
	pushq %rsi
	pushq %rdx
	pushq %rcx
	pushq %r8
	pushq %r9
# 36:visitVariable: l
# 36:visitInt: 103
	movq $103, %r11
	call _myPush
	call _myPop
	movq %rax, -8(%rbp)
# 38:visitVariable: change1
# 38:Reassign: l:=l+1
# 38:visitAdd: l+1
# 38:visitID: l
	movq -8(%rbp), %r11
	call _myPush
# 38:visitInt: 1
	movq $1, %r11
	call _myPush
	call _myPop
	movq %rax, %rbx
	call _myPop
	addq %rbx, %rax
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, -8(%rbp)
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, -16(%rbp)
# 39:visitVariable: access1
# 39:visitFunction: printint(l)
# 39:visitID: l
	movq -8(%rbp), %r11
	call _myPush
	call _myPop
	movq %rax, %rdi
	call printint
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, -24(%rbp)
# 41:visitVariable: change2
# 41:Reassign: kk:=kk+1
# 41:visitAdd: kk+1
# 41:visitID: kk
	movq 16(%rbp), %r11
	call _myPush
# 41:visitInt: 1
	movq $1, %r11
	call _myPush
	call _myPop
	movq %rax, %rbx
	call _myPop
	addq %rbx, %rax
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, 16(%rbp)
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, -32(%rbp)
# 42:visitVariable: access2
# 42:visitFunction: printint(kk)
# 42:visitID: kk
	movq 16(%rbp), %r11
	call _myPush
	call _myPop
	movq %rax, %rdi
	call printint
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, -40(%rbp)
# 44:visitVariable: change5
# 44:Reassign: glob:=glob+1
# 44:visitAdd: glob+1
# 44:visitID: glob
	movq _glob(%rip), %r11
	call _myPush
# 44:visitInt: 1
	movq $1, %r11
	call _myPush
	call _myPop
	movq %rax, %rbx
	call _myPop
	addq %rbx, %rax
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, _glob(%rip)
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, -48(%rbp)
# 45:visitVariable: access5
# 45:visitFunction: printint(glob)
# 45:visitID: glob
	movq _glob(%rip), %r11
	call _myPush
	call _myPop
	movq %rax, %rdi
	call printint
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, -56(%rbp)
# 47:visitVariable: change6
# 47:Reassign: c:=c+1
# 47:visitAdd: c+1
# 47:visitID: c
	movq -88(%rbp), %r11
	call _myPush
# 47:visitInt: 1
	movq $1, %r11
	call _myPush
	call _myPop
	movq %rax, %rbx
	call _myPop
	addq %rbx, %rax
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, -88(%rbp)
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, -64(%rbp)
# 48:visitVariable: access6
# 48:visitFunction: printint(c)
# 48:visitID: c
	movq -88(%rbp), %r11
	call _myPush
	call _myPop
	movq %rax, %rdi
	call printint
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, -72(%rbp)
# 50:visitFunction: printstring("End of function getX\n")
# 50:visitString: "End of function getX\n"
.data
_str2:
.string "End of function getX\n"
.text
	leaq _str2(%rip), %r11
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
# 52:visitMethod: start():=letloc:=newPoint(6,4)letcall1:=loc.getX(1,2,3,4,5,6,7,8)letcall2:=getX(1,2,3,4,5,6,7)printstring("End of function Start\n")
.global my_start
my_start:
	pushq %rbp
	movq %rsp, %rbp
	subq $32, %rsp
	pushq %rdi
	pushq %rsi
	pushq %rdx
	pushq %rcx
	pushq %r8
	pushq %r9
# 53:visitVariable: loc
# 53:visitNew: newPoint(6,4)
# 53:visitInt: 6
	movq $6, %r11
	call _myPush
# 53:visitInt: 4
	movq $4, %r11
	call _myPush
	movq $88, %rdi
	call allocateMem
	movq $VFTPoint, (%rax)
	movq %rax, %r11
	call _myPush
	call c_Point
	movq %rax, %r11
	call _myPush
	call _myPop
	movq %rax, -8(%rbp)
# 54:visitVariable: call1
# 54:visitFunction: loc.getX(1,2,3,4,5,6,7,8)
# 54:visitID: loc
	movq -8(%rbp), %r11
	call _myPush
# 54:visitInt: 1
	movq $1, %r11
	call _myPush
# 54:visitInt: 2
	movq $2, %r11
	call _myPush
# 54:visitInt: 3
	movq $3, %r11
	call _myPush
# 54:visitInt: 4
	movq $4, %r11
	call _myPush
# 54:visitInt: 5
	movq $5, %r11
	call _myPush
# 54:visitInt: 6
	movq $6, %r11
	call _myPush
# 54:visitInt: 7
	movq $7, %r11
	call _myPush
# 54:visitInt: 8
	movq $8, %r11
	call _myPush
	movq $0, %rax
	pushq %rax
	call _myPop
	pushq %rax
	call _myPop
	pushq %rax
	call _myPop
	pushq %rax
	call _myPop
	movq %rax, %r9
	call _myPop
	movq %rax, %r8
	call _myPop
	movq %rax, %rcx
	call _myPop
	movq %rax, %rdx
	call _myPop
	movq %rax, %rsi
	call _myPop
	movq %rax, %rdi
	call nullCheck
	movq (%rax), %r12
	call 8(%r12)
	movq %rax, %r11
	call _myPush
	addq $32, %rsp
	call _myPop
	movq %rax, -16(%rbp)
# 55:visitVariable: call2
# 55:visitFunction: getX(1,2,3,4,5,6,7)
# 55:visitInt: 1
	movq $1, %r11
	call _myPush
# 55:visitInt: 2
	movq $2, %r11
	call _myPush
# 55:visitInt: 3
	movq $3, %r11
	call _myPush
# 55:visitInt: 4
	movq $4, %r11
	call _myPush
# 55:visitInt: 5
	movq $5, %r11
	call _myPush
# 55:visitInt: 6
	movq $6, %r11
	call _myPush
# 55:visitInt: 7
	movq $7, %r11
	call _myPush
	movq $0, %rax
	pushq %rax
	call _myPop
	pushq %rax
	call _myPop
	movq %rax, %r9
	call _myPop
	movq %rax, %r8
	call _myPop
	movq %rax, %rcx
	call _myPop
	movq %rax, %rdx
	call _myPop
	movq %rax, %rsi
	call _myPop
	movq %rax, %rdi
	call my_getX
	movq %rax, %r11
	call _myPush
	addq $16, %rsp
	call _myPop
	movq %rax, -24(%rbp)
# 57:visitFunction: printstring("End of function Start\n")
# 57:visitString: "End of function Start\n"
.data
_str3:
.string "End of function Start\n"
.text
	leaq _str3(%rip), %r11
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
.data
.comm _glob, 8, 8
.text
# 1:visitInt: 100
	movq $100, %r11
	call _myPush
	call _myPop
	movq %rax, _glob(%rip)
	call my_start
	movq %rbp, %rsp
	popq %rbp
	ret

