        .globl  f                       
f:      enter   $0,$0                   # Start function f
        movl    8(%ebp),%eax            # a
        jmp     .exit$f                 # Return-statement
.exit$f:                                
        leave                           
        ret                             # End function f
        .globl  main                    
main:   enter   $0,$0                   # Start function main
        movl    $2,%eax                 # 2
        pushl   %eax                    # Push parameter #1
        movl    $1,%eax                 # 1
        pushl   %eax                    # Push parameter #2
        call    f                       # Call f
        addl    $4,%esp                 # Remove parameters
        pushl   %eax                    # Push parameter #1
        call    putint                  # Call putint
        addl    $4,%esp                 # Remove parameters
        movl    $10,%eax                # 10
        pushl   %eax                    # Push parameter #1
        call    putchar                 # Call putchar
        addl    $4,%esp                 # Remove parameters
.exit$main:                                
        leave                           
        ret                             # End function main
