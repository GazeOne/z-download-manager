package com.wy.download

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.wy.download.addtask.AddTaskActivity
import com.wy.download.home.EbookScreen
import com.wy.download.home.HomeScreen
import com.wy.download.home.MoviesScreen
import com.wy.download.home.MusicScreen
import com.wy.download.home.NavigationItem
import com.wy.download.home.SettingsScreen
import com.wy.download.ui.theme.ZdownloadmanagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZdownloadmanagerTheme {
                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { TopBar(navController) },
                    bottomBar = { BottomBar(navController) },
                    floatingActionButton = { FloatingButton() }
                ) { innerPadding ->
                    Navigation(navController, innerPadding)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavHostController) {
    val currentRoute = getCurrentRoute(navController)
    val title = when(currentRoute) {
        NavigationItem.Home.route -> NavigationItem.Home.title
        NavigationItem.Music.route -> NavigationItem.Music.title
        NavigationItem.Ebook.route -> NavigationItem.Ebook.title
        NavigationItem.Movies.route -> NavigationItem.Movies.title
        NavigationItem.Settings.route -> NavigationItem.Settings.title
        else -> "Download Manager"
    }

    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text(
                title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = { /* do something */ }) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = stringResource(R.string.homepage_menu)
                )
            }
        },
        actions = {
            IconButton(onClick = { /* do something */ }) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Localized description"
                )
            }
        },
    )
}

@Composable
fun BottomBar(navController: NavHostController) {
    val items = listOf(
        NavigationItem.Home,
        NavigationItem.Movies,
        NavigationItem.Music,
        NavigationItem.Ebook,
        NavigationItem.Settings
    )
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.title
                    )
                },
                label = { Text(text = item.title) },
                alwaysShowLabel = true,
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun FloatingButton() {
    val context = LocalContext.current
    FloatingActionButton(onClick = {
        val intent = Intent(context, AddTaskActivity::class.java)
        context.startActivity(intent)
    }) {
        Icon(Icons.Default.Add, contentDescription = "Add")
    }
}

@Composable
fun Navigation(navController: NavHostController, padding: PaddingValues) {
    NavHost(
        navController,
        startDestination = NavigationItem.Home.route,
        modifier = Modifier.padding(padding)
    ) {
        composable(NavigationItem.Home.route) {
            HomeScreen()
        }
        composable(NavigationItem.Movies.route) {
            MoviesScreen()
        }
        composable(NavigationItem.Music.route) {
            MusicScreen()
        }
        composable(NavigationItem.Ebook.route) {
            EbookScreen()
        }
        composable(NavigationItem.Settings.route) {
            SettingsScreen()
        }
    }
}

@Composable
fun getCurrentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}