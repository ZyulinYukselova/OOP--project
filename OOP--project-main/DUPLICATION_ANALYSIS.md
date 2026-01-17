# Анализ на повторяем код в DistributorService

## Намерени повторения:

### 1. ✅ ОБЩА ЛОГИКА ЗА ОБНОВЯВАНЕ НА ПРОФИЛИ
Повтаря се в:
- `updateDistributor()` (линии 78-89)
- `updateCashier()` (линии 114-125)
- `CompanyService.updateCompany()` (линии 38-49)

**Повтарящ се код:**
```java
if (name != null && !name.trim().isEmpty()) {
    entity.setName(name.trim());
}
if (commission != null) {
    if (commission < 0) {
        throw new ValidationException("Commission cannot be negative");
    }
    entity.setCommission(commission);
}
if (contact != null && !contact.trim().isEmpty()) {
    entity.setContact(contact.trim());
}
```

### 2. ⚠️ ПОХОЖА ЛОГИКА ЗА ПРОВЕРКА НА ПРАВА
В `updateDistributor()` и `updateCashier()` има сходна логика за проверка на права, но с различни условия, така че това е по-малък проблем.

### 3. ✅ ПАТТЕРН ЗА НАМИРАНЕ И ВАЛИДАЦИЯ
Методите `getDistributor()` и `getCashier()` имат идентична логика.

---

## ПРЕПОРЪКА ЗА РЕФАКТОРИРАНЕ:

1. Създаване на helper метод за обновяване на профилни полета
2. Извличане на обща логика за валидация на комисионна
