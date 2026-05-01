package com.sandeep.personalfinancecompanion.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.sandeep.personalfinancecompanion.R
import com.sandeep.personalfinancecompanion.domain.model.Category
import java.util.Calendar

object LocalizationUtils {
    @Composable
    fun getCategoryName(category: Category): String {
        return stringResource(
            when (category) {
                Category.FOOD -> R.string.cat_food
                Category.TRANSPORT -> R.string.cat_transport
                Category.SHOPPING -> R.string.cat_shopping
                Category.ENTERTAINMENT -> R.string.cat_entertainment
                Category.BILLS -> R.string.cat_bills
                Category.HEALTH -> R.string.cat_health
                Category.EDUCATION -> R.string.cat_education
                Category.SALARY -> R.string.cat_salary
                Category.FREELANCE -> R.string.cat_freelance
                Category.INVESTMENT -> R.string.cat_investment
                Category.GIFT -> R.string.cat_gift
                Category.UDHAAR -> R.string.cat_udhaar
                Category.OTHER -> R.string.cat_other
            }
        )
    }

    @Composable
    fun getDayName(dayOfWeek: Int): String {
        return stringResource(
            when (dayOfWeek) {
                Calendar.MONDAY -> R.string.day_monday
                Calendar.TUESDAY -> R.string.day_tuesday
                Calendar.WEDNESDAY -> R.string.day_wednesday
                Calendar.THURSDAY -> R.string.day_thursday
                Calendar.FRIDAY -> R.string.day_friday
                Calendar.SATURDAY -> R.string.day_saturday
                Calendar.SUNDAY -> R.string.day_sunday
                else -> R.string.app_name // Fallback
            }
        )
    }
    
    @Composable
    fun translateDayName(name: String): String {
        return when (name) {
            "Monday" -> stringResource(R.string.day_monday)
            "Tuesday" -> stringResource(R.string.day_tuesday)
            "Wednesday" -> stringResource(R.string.day_wednesday)
            "Thursday" -> stringResource(R.string.day_thursday)
            "Friday" -> stringResource(R.string.day_friday)
            "Saturday" -> stringResource(R.string.day_saturday)
            "Sunday" -> stringResource(R.string.day_sunday)
            else -> name
        }
    }

    @Composable
    fun translateCategoryName(name: String): String {
        return when (name) {
            "Food" -> stringResource(R.string.cat_food)
            "Transport" -> stringResource(R.string.cat_transport)
            "Shopping" -> stringResource(R.string.cat_shopping)
            "Entertainment" -> stringResource(R.string.cat_entertainment)
            "Bills" -> stringResource(R.string.cat_bills)
            "Health" -> stringResource(R.string.cat_health)
            "Education" -> stringResource(R.string.cat_education)
            "Salary" -> stringResource(R.string.cat_salary)
            "Freelance" -> stringResource(R.string.cat_freelance)
            "Investment" -> stringResource(R.string.cat_investment)
            "Gift" -> stringResource(R.string.cat_gift)
            "Lent & Borrowed" -> stringResource(R.string.cat_udhaar)
            "Other" -> stringResource(R.string.cat_other)
            else -> name
        }
    }

    @Composable
    fun translateGoalTitle(title: String): String {
        return when (title) {
            "New Car" -> stringResource(R.string.goal_new_car)
            "Dream Home" -> stringResource(R.string.goal_dream_home)
            "New Phone" -> stringResource(R.string.goal_new_phone)
            "Education" -> stringResource(R.string.goal_education)
            "Retirement" -> stringResource(R.string.goal_retirement)
            else -> title
        }
    }
}
