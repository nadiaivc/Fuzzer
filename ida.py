from idaapi import *

def printCallSource(addr):
	
	print 'Function [%s] %s is called from:' % (hex(addr), GetFunctionName(addr))
	
	refs = CodeRefsTo(addr, 0)
	for r in refs:		
		print "[%s] %s" % (hex(r), GetFunctionName(r))
	print '\n' 
	
	
unsafeFunctions = (
    'gets',
	'scanf',
	'fgetc',
	'getc',
	'fread',
	'fgets',
	'fscanf'

	'memset',
	'memcpy',
	'strcpy',
	'strcat',
    
	'sprintf',
	'vsprintf',
	'sscanf',
    
	'malloc',
	'calloc',
	'realloc',
	'free'
)


def listFunctions(ea):
	unsafeFuncMap = {}
	for function_ea in Functions(SegStart(ea), SegEnd(ea)):
		
		funcName = GetFunctionName(function_ea)
		funcAddr = function_ea
		
		if funcName in unsafeFunctions:
			unsafeFuncMap[funcName] = funcAddr
			
	return unsafeFuncMap
			
autoWait()
ea = ScreenEA()
print '*' * 60
print 'Start'
print '*' * 60
unsafe = listFunctions(ea)

print 'Unsafe functions:'
for e in unsafe.values():
	printCallSource(e)	


print 'Script end'