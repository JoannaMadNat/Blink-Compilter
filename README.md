# Phase Five - Áfanga Fimm
<b>Level Completed:</b> A Level + Optimization<br>
<b>Target Platform:</b> Linux/Ubuntu
### Time Spent
<b>Phase 5 EC:</b> 06:13‬ hours  <br>
<b>Phase 5:</b> 61:14‬ hours  <br>
<b>Phase 4:</b> 33:33‬ hours  <br>
<b>Phase 3:</b> 18:51‬ hours  <br>
<b>Phase 2:</b> 00:10‬ hours  <br>
<b>Phase 2 CP:</b> 10:00‬ hours <br>
<b>Phase 1:</b> 14:22‬ hours <br>
<b>Total Time:</b> 144 Hours

### Command Line Options
* -ds : Print Lexer Output
* -dp : Print Parser Output
* -S  : Produce only Assembly file without executable
* -r  : Run executable after producing it (Ignored if run with -S)
* -z  : Disable parse tree optimizer
* -d  : Delete produced assembly and executable after operation is done
* -o \<NAME\> : Set custom name for executable

### Extra Credit Features
The Parse Tree Optimizer takes the created parse tree from the semantic checked and prunes it.
Things that don't need to happen in runtime are processed at compile time, such as number operations and string concatinations.

What it does is go into the expression node where the operation happens and replaces itself with a simple int node or string node, depending on what it is.
For example, something like 7 + (8 * 2) / ~1 - 100 will normally use multiple unnecessary branches of operations. 
Through optimization, the root node of all these expressions is replaced with an int node with the value "−109" and sent out to CodeGen.
<br> 
The same applies to simple strings, any simple strings will be merged together along with integers;
```
let y := "but only after dark...\n"
before optimization -> let k := "City fog " & 23000 & "somewhere in those lights you're on your " & 800 & "phone\n" & y
after optimization -> let k := "City fog 23000somewhere in those lights you're on your 800phone\n" & y
```
### Academic Integrity Statement
I certify that the accompanying work represents my own 
intellectual effort. Furthermore, I have received no 
outside help other than what is documented below and/or 
in program source code comments.
 
#### Consulted Sources
  [Bónus demo video link](https://bju-my.sharepoint.com/:v:/g/personal/jalma146_students_bju_edu/EcdFAZdLQNFLuP3T3xiubMsBVJcaoOsy4sP8h6uHIyWyQw?e=aXo8fF)
- Bónus: Base code fome https://github.com/kasik96/Swift-VS-Code
- Bónus: Regex for strings https://stackoverflow.com/questions/5695240/php-regex-to-ignore-escaped-quotes-within-quotes/5696141
- Viking ship: https://ascii.co.uk/art/viking
![Qwek](stdlib/external-content.duckduckgo.com.jpeg)
