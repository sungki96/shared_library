package com.demo.common

import groovy.json.*

def red(String script) {

	ansiColor('xterm') {
		sh "set +x; echo -e \"\\033[1;031m${script} \\033[0m\""
	}
}

def green(String script) {

	ansiColor('xterm') {
		sh "set +x; echo -e \"\\033[1;032m${script} \\033[0m\""
	}
}

def blue(String script) {

	ansiColor('xterm') {
		sh "set +x; echo -e \"\\033[1;034m${script} \\033[0m\""
	}
}

def summaryRed(String script) {

	ansiColor('xterm') {
		sh "set +x; echo -e \"\\033[1;031m${script} \\033[0m\""
	}
}

def summaryGreen(String script) {

	ansiColor('xterm') {
		sh "set +x; echo -e \"\\033[1;032m${script} \\033[0m\""
	}
}
