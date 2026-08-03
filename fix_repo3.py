with open('app/src/main/java/com/example/data/ExpenseRepository.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if line.strip() == "}":
        pass  # skip all standalone braces, we'll add one at the end
    else:
        new_lines.append(line)

new_lines.append("}\n")
with open('app/src/main/java/com/example/data/ExpenseRepository.kt', 'w') as f:
    f.writelines(new_lines)
