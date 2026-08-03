with open('app/src/main/java/com/example/data/ExpenseRepository.kt', 'r') as f:
    text = f.read()

text = text.replace("    }\n}\n    fun getInvestments", "    }\n    fun getInvestments")

with open('app/src/main/java/com/example/data/ExpenseRepository.kt', 'w') as f:
    f.write(text)
