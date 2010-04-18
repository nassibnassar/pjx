all:
	@echo "This Makefile does not do much of anything right now."

changelog:
	rcs2log -h sourceforge > ./ChangeLog
