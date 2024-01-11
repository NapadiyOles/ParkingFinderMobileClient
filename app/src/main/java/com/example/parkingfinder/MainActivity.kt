package com.example.parkingfinder

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.parkingfinder.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val userToken = sharedPreferences.getString("UserToken", null)
        val status = sharedPreferences.getString("UserStatus", null)

        if (userToken == null) {
            // Navigate to Login Fragment
            navController.navigate(R.id.loginFragment)
        } else if (status == null) {
            // Navigate to Profile Fragment
            navController.navigate(R.id.profileFragment)
        } else {
            navController.navigate( when (status) {
                "Away" -> {
                    R.id.profileFragment
                }
                "Parking" -> {
                    R.id.parkingFragment
                }
                "Leaving" -> {
                    R.id.leavingFragment
                }

                else -> {
                    R.id.profileFragment
                }
            })
        }
    }
}
//
//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    ParkingFinderTheme {
//        Greeting("Android")
//    }
//}