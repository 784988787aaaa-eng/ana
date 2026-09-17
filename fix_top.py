import re

with open('app/src/main/java/com/smartledger/aldaftar/ui/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace("DomainRepositories.SettingsRepository", "SettingsRepository")
content = content.replace("DomainRepositories.CategoriesRepository", "CategoriesRepository")
content = content.replace("DomainRepositories.TrashRepository", "TrashRepository")
content = content.replace("DomainRepositories.HabayebRepository", "HabayebRepository")
content = content.replace("import com.smartledger.aldaftar.data.repository.DomainRepositories", "import com.smartledger.aldaftar.data.repository.*")

with open('app/src/main/java/com/smartledger/aldaftar/ui/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content)
