import re

with open('app/src/main/res/values/strings.xml', 'r') as f:
    content = f.read()

# Add missing strings
if "toast_trash_emptied" not in content:
    content = content.replace("</resources>", """
    <string name="toast_trash_emptied">تم إفراغ سلة المهملات بنجاح</string>
    <string name="toast_item_restored">تمت الاستعادة بنجاح</string>
    <string name="toast_item_permanently_deleted">تم الحذف نهائياً</string>
    <string name="toast_items_restored">تمت الاستعادة بنجاح</string>
    <string name="toast_items_permanently_deleted">تم الحذف نهائياً</string>
</resources>""")

with open('app/src/main/res/values/strings.xml', 'w') as f:
    f.write(content)
