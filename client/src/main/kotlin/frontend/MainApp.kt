package frontend

import tornadofx.App

class MainApp : App(MainView::class, Styles::class)

// - pending    red
// - mine       green
// - read       white
// - unread     yellow