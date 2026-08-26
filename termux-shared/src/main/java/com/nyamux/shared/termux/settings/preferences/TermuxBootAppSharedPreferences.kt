package com.nyamux.shared.termux.settings.preferences

import android.content.Context
import com.nyamux.shared.android.PackageUtils
import com.nyamux.shared.logger.Logger
import com.nyamux.shared.settings.preferences.AppSharedPreferences
import com.nyamux.shared.settings.preferences.SharedPreferenceUtils
import com.nyamux.shared.termux.TermuxConstants
import com.nyamux.shared.termux.TermuxUtils
import com.nyamux.shared.termux.settings.preferences.TermuxPreferenceConstants.TERMUX_BOOT_APP

class TermuxBootAppSharedPreferences private constructor(context: Context) : AppSharedPreferences(
    context,
    SharedPreferenceUtils.getPrivateSharedPreferences(
        context,
        TermuxConstants.TERMUX_BOOT_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION
    ),
    SharedPreferenceUtils.getPrivateAndMultiProcessSharedPreferences(
        context,
        TermuxConstants.TERMUX_BOOT_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION
    )
) {

    fun getLogLevel(readFromFile: Boolean): Int {
        return if (readFromFile) {
            SharedPreferenceUtils.getInt(
                mMultiProcessSharedPreferences,
                TERMUX_BOOT_APP.KEY_LOG_LEVEL,
                Logger.DEFAULT_LOG_LEVEL
            )
        } else {
            SharedPreferenceUtils.getInt(
                mSharedPreferences,
                TERMUX_BOOT_APP.KEY_LOG_LEVEL,
                Logger.DEFAULT_LOG_LEVEL
            )
        }
    }

    fun setLogLevel(context: Context, logLevel: Int, commitToFile: Boolean) {
        val updatedLogLevel = Logger.setLogLevel(context, logLevel)
        SharedPreferenceUtils.setInt(
            mSharedPreferences,
            TERMUX_BOOT_APP.KEY_LOG_LEVEL,
            updatedLogLevel,
            commitToFile
        )
    }

    companion object {
        private const val LOG_TAG = "TermuxBootAppSharedPreferences"

        /**
         * Get {@link TermuxBootAppSharedPreferences}.
         *
         * @param context The {@link Context} to use to get the {@link Context} of the
         *                {@link TermuxConstants#TERMUX_BOOT_PACKAGE_NAME}.
         * @return Returns the {@link TermuxBootAppSharedPreferences}. This will {@code null} if an exception is raised.
         */
        @JvmStatic
        fun build(context: Context): TermuxBootAppSharedPreferences? {
            val termuxBootPackageContext = PackageUtils.getContextForPackage(
                context,
                TermuxConstants.TERMUX_BOOT_PACKAGE_NAME
            ) ?: return null
            return TermuxBootAppSharedPreferences(termuxBootPackageContext)
        }

        /**
         * Get {@link TermuxBootAppSharedPreferences}.
         *
         * @param context The {@link Context} to use to get the {@link Context} of the
         *                {@link TermuxConstants#TERMUX_BOOT_PACKAGE_NAME}.
         * @param exitAppOnError If {@code true} and failed to get package context, then a dialog will
         *                       be shown which when dismissed will exit the app.
         * @return Returns the {@link TermuxBootAppSharedPreferences}. This will {@code null} if an exception is raised.
         */
        @JvmStatic
        fun build(context: Context, exitAppOnError: Boolean): TermuxBootAppSharedPreferences? {
            val termuxBootPackageContext = TermuxUtils.getContextForPackageOrExitApp(
                context,
                TermuxConstants.TERMUX_BOOT_PACKAGE_NAME,
                exitAppOnError
            ) ?: return null
            return TermuxBootAppSharedPreferences(termuxBootPackageContext)
        }
    }
}
