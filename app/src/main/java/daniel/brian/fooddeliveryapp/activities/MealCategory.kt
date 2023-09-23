@file:Suppress("DEPRECATION")

package daniel.brian.fooddeliveryapp.activities

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import daniel.brian.fooddeliveryapp.adapters.OrdinaryDrinksAdapter
import daniel.brian.fooddeliveryapp.databinding.ActivityMealCategoryBinding
import daniel.brian.fooddeliveryapp.db.MealDataBase
import daniel.brian.fooddeliveryapp.fragments.HomeFragment
import daniel.brian.fooddeliveryapp.pojo.Drink
import daniel.brian.fooddeliveryapp.pojo.Meal
import daniel.brian.fooddeliveryapp.util.shortenName
import daniel.brian.fooddeliveryapp.viewmodel.DrinksViewModel
import daniel.brian.fooddeliveryapp.viewmodel.MealDetailsViewModel
import daniel.brian.fooddeliveryapp.viewmodel.MealDetailsViewModelFactory

class MealCategory : AppCompatActivity() {
    private lateinit var mealId: String
    private lateinit var mealName: String
    private lateinit var mealThumb: String
    private lateinit var drinkThumb: String
    private lateinit var binding: ActivityMealCategoryBinding
    private lateinit var drinksMvvm: DrinksViewModel
    private lateinit var mealMvvm: MealDetailsViewModel
    private var mealToSave: Meal? = null
    private lateinit var ordinaryDrinksAdapter: OrdinaryDrinksAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT
        binding = ActivityMealCategoryBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val mealDataBase = MealDataBase.getInstance(this)
        val mealDetailsViewModelFactory = MealDetailsViewModelFactory(mealDataBase)

        getMealInformation()

        mealMvvm =
            ViewModelProvider(this, mealDetailsViewModelFactory)[MealDetailsViewModel::class.java]
        mealMvvm.getMealDetails(mealId)
        observeMealDetailsLiveData()
        onClickFavoriteMeal()

        drinksMvvm = ViewModelProvider(this)[DrinksViewModel::class.java]
        ordinaryDrinksAdapter = OrdinaryDrinksAdapter()

        prepareAddsOnRecyclerView()

        drinksMvvm.getDrinksCategories()
        observeOrdinaryDrinksLiveData()

        setInformationInViews()

        binding.backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun onClickFavoriteMeal() {
        binding.favoriteMeal.setOnClickListener {
            binding.favoriteMeal.setColorFilter(Color.parseColor("#df3079"))
            mealToSave?.let {
                mealMvvm.insertMeal(it)
                Toast.makeText(this, "Meal saved to favorites", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeMealDetailsLiveData() {
        mealMvvm.observeMealDetailsLiveData().observe(this) { value ->
            mealToSave = value
            val image = mealToSave!!.strMealThumb
            Glide.with(this)
                .load(image)
                .into(binding.imageMeal)
        }
    }

    private fun prepareAddsOnRecyclerView() {
        binding.addsOnRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(this@MealCategory, LinearLayoutManager.HORIZONTAL, false)
            adapter = ordinaryDrinksAdapter
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun observeOrdinaryDrinksLiveData() {
        drinksMvvm.observeOrdinaryDrinksLiveData().observe(this) { drinkList ->
            (drinkList as? ArrayList<Drink>)?.let {
                ordinaryDrinksAdapter.setOrdinaryDrinks(
                    drinksList = it,
                )
            }
        }
    }

    private fun setInformationInViews() {
        Glide.with(applicationContext)
            .load(mealThumb)
            .into(binding.imageMeal)

        binding.mealName.text = mealName.shortenName()
    }

    private fun getMealInformation() {
        val intent = intent
        mealId = intent.getStringExtra(HomeFragment.MEAL_ID)!!
        mealName = intent.getStringExtra(HomeFragment.MEAL_NAME)!!.shortenName()
        mealThumb = intent.getStringExtra(HomeFragment.MEAL_THUMB)!!
    }
}
