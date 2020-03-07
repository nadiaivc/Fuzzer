#define _CRT_SECURE_NO_WARNINGS
#include "CLoader.h"
#include <iostream>
#include <windows.h>
#include <stdio.h>

void registers_output(CONTEXT* cont, const char* error)
{
	FILE* file = fopen("C:\\Users\\Win10\\IdeaProjects\\fuzzer\\log.txt", "a");
	fprintf(file, "Exception: %s\n", error);
	fprintf(file, "RAX  :  0x%p\n", (void*)cont->Rax);
	fprintf(file, "RBX  :  0x%p\n", (void*)cont->Rbx);
	fprintf(file, "RCX  :  0x%p\n", (void*)cont->Rcx);
	fprintf(file, "RDX  :  0x%p\n", (void*)cont->Rdx);
	fprintf(file, "RIP  :  0x%p\n", (void*)cont->Rip);
	fprintf(file, "RSP  :  0x%p\n", (void*)cont->Rsp);
	fprintf(file, "RBP  :  0x%p\n", (void*)cont->Rbp);
	fprintf(file, "RDI  :  0x%p\n", (void*)cont->Rdi);
	fprintf(file, "RSI  :  0x%p\n", (void*)cont->Rsi);
	fprintf(file, "FLG  :  0x%p\n\n", (void*)cont->EFlags);
	fclose(file);
}

JNIEXPORT jint JNICALL Java_CLoader_IsDebug
(JNIEnv*, jobject) {
	PROCESS_INFORMATION pi;
	STARTUPINFO si;
	DEBUG_EVENT debug_event = { 0 };
	HANDLE thread;
	CONTEXT cont;
	BOOL status;
	ZeroMemory(&pi, sizeof(pi));
	ZeroMemory(&si, sizeof(si));
	si.cb = sizeof(si);
	status = CreateProcessA((char*)"vuln6.exe", NULL, NULL, NULL, FALSE, DEBUG_PROCESS, NULL, NULL, &si, &pi);
	if (status == false)
	{
		printf("CreateProcess failed: %d\n", GetLastError());
		return 0;
	}
	while (1)
	{
		status = WaitForDebugEvent(&debug_event, 500);
		if (status == false)
		{
			if (GetLastError() != ERROR_SEM_TIMEOUT)
				printf("WaitForDebugEvent failed: %d\n", GetLastError());
			break;
		}
		if (debug_event.dwDebugEventCode != EXCEPTION_DEBUG_EVENT)
		{
			ContinueDebugEvent(debug_event.dwProcessId, debug_event.dwThreadId, DBG_CONTINUE);
			continue;
		}
		thread = OpenThread(THREAD_ALL_ACCESS, FALSE, debug_event.dwThreadId);
		if (thread == NULL)
		{
			printf("OpenThread failed: %d\n", GetLastError());
			break;
		}
		cont.ContextFlags = CONTEXT_FULL;
		status = GetThreadContext(thread, &cont);
		if (status == false)
		{
			printf("GetThreadContext failed: %d\n", GetLastError());
			CloseHandle(thread);
			break;
		}

		switch (debug_event.u.Exception.ExceptionRecord.ExceptionCode)
		{
		case EXCEPTION_ACCESS_VIOLATION:
			registers_output(&cont, "Access Violation\n");
			CloseHandle(pi.hProcess);
			return(1);
			break;
		case EXCEPTION_STACK_OVERFLOW:
			registers_output(&cont, "Stack Overflow\n");
			CloseHandle(pi.hProcess);
			return(2);
			break;
		case 0x80000003:
			ContinueDebugEvent(debug_event.dwProcessId, debug_event.dwThreadId, DBG_CONTINUE);
			break;
		default:
			ContinueDebugEvent(debug_event.dwProcessId, debug_event.dwThreadId, DBG_CONTINUE);
		}
	}
	CloseHandle(pi.hProcess);
	return 3;
}