package com.arny.flightlogbook.presentation.main

/*class MainActivity : AppCompatActivity(), HasAndroidInjector {
    private companion object {
        const val DRAWER_SELECTION = "drawer_selection"
        const val TIME_DELAY = 2000
    }

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>
    private lateinit var binding: ActivityHomeBinding
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var toolbar: Toolbar
    private var backPressedTime: Long = 0

    override fun androidInjector(): AndroidInjector<Any> = androidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater);
        setContentView(binding.root);
        toolbar = findViewById(R.id.home_toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = getString(R.string.fragment_logbook)
        actionBarDrawerToggle = ActionBarDrawerToggle(
            this,
            binding.dlMain,
            toolbar,
            R.string.openNavDrawer,
            R.string.closeNavDrawer
        )
        binding.dlMain.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.isDrawerIndicatorEnabled = true
        actionBarDrawerToggle.syncState()
        binding.navViewMain.setNavigationItemSelectedListener { item ->
            val navItem = toNavigateItem(item)
            if (navItem != -1L) {
                selectItem(navItem)
                binding.dlMain.closeDrawers()
                true
            } else {
                false
            }
        }
        if (savedInstanceState == null) {
            selectItem(NavigateItems.MENU_FLIGHTS.index)
        } else {
            try {
                savedInstanceState.getString(DRAWER_SELECTION)?.toLong()?.let { index ->
                    toMenuItem(index)?.let { binding.navViewMain.setCheckedItem(it) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun toNavigateItem(item: MenuItem?) = when (item?.itemId) {
        R.id.menu_flights -> NavigateItems.MENU_FLIGHTS.index
        R.id.menu_flight_types -> NavigateItems.MENU_FLIGHT_TYPES.index
        R.id.menu_plane_types -> NavigateItems.MENU_PLANE_TYPES.index
        R.id.menu_fields -> NavigateItems.MENU_CUSTOM_FIELDS.index
        R.id.menu_airports -> NavigateItems.MENU_AIRPORTS.index
        R.id.menu_settings -> NavigateItems.MENU_SETTINGS.index
        R.id.menu_stats -> NavigateItems.MENU_STATS.index
        else -> -1
    }

    override fun onReturnResult(intent: Intent?, resultCode: Int) {
        setResult(resultCode, intent)
    }

    override fun setResultToTargetFragment(
        currentFragment: Fragment,
        intent: Intent?,
        resultCode: Int
    ) {
        currentFragment.targetFragment?.onActivityResult(
            currentFragment.targetRequestCode,
            resultCode,
            intent
        )
    }

    private fun toMenuItem(index: Long) = when (index) {
        NavigateItems.MENU_FLIGHTS.index -> R.id.menu_flights
        NavigateItems.MENU_FLIGHT_TYPES.index -> R.id.menu_flight_types
        NavigateItems.MENU_PLANE_TYPES.index -> R.id.menu_plane_types
        NavigateItems.MENU_CUSTOM_FIELDS.index -> R.id.menu_fields
        NavigateItems.MENU_AIRPORTS.index -> R.id.menu_airports
        NavigateItems.MENU_SETTINGS.index -> R.id.menu_settings
        NavigateItems.MENU_STATS.index -> R.id.menu_stats
        else -> null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (binding.dlMain.getDrawerLockMode(GravityCompat.START) != LOCK_MODE_UNLOCKED) {
                    onBackPressed()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(DRAWER_SELECTION, toNavigateItem(binding.navViewMain.checkedItem).toString())
        super.onSaveInstanceState(outState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        for (fragment in supportFragmentManager.fragments) {
            fragment.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun navigateTo(
        item: NavigateItems,
        addToBackStack: Boolean,
        bundle: Bundle?,
        targetFragment: Fragment?,
        requestCode: Int?
    ) {
        selectItem(item.index, addToBackStack, bundle, targetFragment, requestCode)
    }

    private fun getFragmentContainer(position: Long, bundle: Bundle?): FragmentContainer? {
        return when (position) {
            NavigateItems.MENU_FLIGHTS.index -> FragmentContainer(
                FlightListFragment.getInstance(),
                "FlightListFragment"
            )
            NavigateItems.MENU_PLANE_TYPES.index -> FragmentContainer(
                PlaneTypesFragment.getInstance(),
                "PlaneTypesFragment"
            )
            NavigateItems.MENU_FLIGHT_TYPES.index -> FragmentContainer(
                FlightTypesFragment.getInstance(),
                "FlightTypesFragment"
            )
            NavigateItems.MENU_CUSTOM_FIELDS.index -> FragmentContainer(
                CustomFieldsListFragment.getInstance(),
                "CustomFieldsListFragment"
            )
            NavigateItems.MENU_STATS.index -> FragmentContainer(StatisticFragment.getInstance(), "StatisticFragment")
            NavigateItems.MENU_SETTINGS.index -> FragmentContainer(SettingsFragment.getInstance(), "SettingsFragment")
            NavigateItems.MENU_AIRPORTS.index -> FragmentContainer(AirportsFragment.getInstance(), "AirportsFragment")
            NavigateItems.EDIT_AIRPORT.index -> {
                launchActivity<FragmentContainerActivity>(CONSTS.REQUESTS.REQUEST_EDIT_AIRPORT) {
                    action = CONSTS.EXTRAS.EXTRA_ACTION_EDIT_AIRPORT
                    bundle?.let { putExtras(it) }
                }
                null
            }
            NavigateItems.ITEM_EDIT_FIELD.index -> {
                launchActivity<FragmentContainerActivity>(CONSTS.REQUESTS.REQUEST_EDIT_CUSTOM_FIELD) {
                    action = CONSTS.EXTRAS.EXTRA_ACTION_EDIT_CUSTOM_FIELD
                    bundle?.let { putExtras(it) }
                }
                null
            }
            NavigateItems.PLANE_TYPE_EDIT.index -> {
                launchActivity<FragmentContainerActivity>(CONSTS.REQUESTS.REQUEST_EDIT_PLANE_TYPE) {
                    action = CONSTS.EXTRAS.EXTRA_ACTION_EDIT_PLANE_TYPE
                    bundle?.let { putExtras(it) }
                }
                null
            }
            else -> null
        }
    }

    private fun selectItem(
        position: Long,
        addToBackStack: Boolean = false,
        bundle: Bundle? = null,
        targetFragment: Fragment? = null,
        requestCode: Int? = null
    ) {
        val fragmentContainer = getFragmentContainer(position, bundle)
        var fragment = getFragmentByTag(fragmentContainer?.tag)
        if (fragment == null) {
            fragment = fragmentContainer?.fragment
        }
        if (fragment != null) {
            if (targetFragment != null) {
                fragment.setTargetFragment(targetFragment, requestCode ?: 0)
            }
            replaceFragment(
                fragment,
                R.id.container,
                addToBackStack,
                fragmentContainer?.tag
            )
        }
        binding.dlMain.closeDrawer(binding.navViewMain)
    }

    override fun onBackPressed() {
        val drawerLayout = binding.dlMain
        if (drawerLayout.isDrawerOpen(binding.navViewMain)) {
            drawerLayout.closeDrawer(binding.navViewMain)
        } else {
            val isMain = supportFragmentManager.fragments.find { curFrag ->
                curFrag is MainFirstFragment && curFrag.isVisible
            } != null
            if (!isMain) {
                selectItem(NavigateItems.MENU_FLIGHTS.index, requestCode = null)
            } else {
                if (backPressedTime + TIME_DELAY > System.currentTimeMillis()) {
                    finish()
                } else {
                    binding.container.showSnackBar(getString(R.string.press_back_again_to_exit))
                }
                backPressedTime = System.currentTimeMillis()
            }
        }
    }
}*/
