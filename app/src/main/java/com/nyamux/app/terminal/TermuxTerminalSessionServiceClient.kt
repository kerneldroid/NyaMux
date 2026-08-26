package com.nyamux.app.terminal

import android.app.Service
import com.nyamux.app.TermuxService
import com.nyamux.shared.termux.shell.command.runner.terminal.TermuxSession
import com.nyamux.shared.termux.terminal.TermuxTerminalSessionClientBase
import com.nyamux.terminal.TerminalSession
import com.nyamux.terminal.TerminalSessionClient

/** The {@link TerminalSessionClient} implementation that may require a {@link Service} for its interface methods. */
class TermuxTerminalSessionServiceClient(private val mService: TermuxService) : TermuxTerminalSessionClientBase() {

    override fun setTerminalShellPid(terminalSession: TerminalSession, pid: Int) {
        val termuxSession: TermuxSession? = mService.getTermuxSessionForTerminalSession(terminalSession)
        if (termuxSession != null) {
            termuxSession.executionCommand.mPid = pid
        }
    }

    companion object {
        private const val LOG_TAG = "TermuxTerminalSessionServiceClient"
    }
}
