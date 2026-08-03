import re

with open('app/src/main/java/com/example/ui/ExpenseViewModel.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if line.strip().startswith('fun addExpense') or \
       line.strip().startswith('fun deleteExpense') or \
       line.strip().startswith('fun addGoal') or \
       line.strip().startswith('fun depositToGoal') or \
       line.strip().startswith('fun deleteGoal') or \
       line.strip().startswith('fun parseExpenseWithAi') or \
       line.strip().startswith('fun parseExpenseImageWithAi') or \
       line.strip().startswith('fun generateMonthlyReport'):
        new_lines.append(line)
        new_lines.append("        viewModelScope.launch {\n")
    elif line.strip().startswith('fun sendChatMessage'):
        new_lines.append(line)
        # we know sendChatMessage has some synchronous code before launch
    else:
        new_lines.append(line)

with open('app/src/main/java/com/example/ui/ExpenseViewModel.kt', 'w') as f:
    f.writelines(new_lines)
