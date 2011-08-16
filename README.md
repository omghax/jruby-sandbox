JRuby Sandbox
=============

The JRuby sandbox is a reimplementation of _why's freaky freaky sandbox
in JRuby, and is heavily based on [javasand][1] by Ola Bini, but updated
for JRuby 1.6.

## Prerequisites

This gem requires JRuby 1.6. You can install it with RVM:

    rvm install jruby-1.6.1

## Building

To build the JRuby extension, run `rake compile`. This will build the
lib/sandbox/sandbox.jar file, which lib/sandbox.rb loads.

## Basic Usage

Sandbox gives you a self-contained JRuby interpreter in which to eval
code without polluting the host environment.

    >> require "sandbox"
    => true
    >> sand = Sandbox::Full.new
    => #<Sandbox::Full:0x46377e2a>
    >> sand.eval("x = 1 + 2")
    => 3
    >> sand.eval("x")
    => 3
    >> x
    NameError: undefined local variable or method `x' for #<Object:0x11cdc190>

There's also Sandbox::Full#require, which lets you invoke Kernel#require
directly for the sandbox, so you can load any trusted core libraries.
Note that this is a direct binding to Kernel#require, so it will only
load ruby stdlib libraries (i.e. no rubygems support yet).

## Known Issues / TODOs

  * Sandbox::Full#import is unfinished.

[1]: http://ola-bini.blogspot.com/2006/12/freaky-freaky-sandbox-has-come-to-jruby.html
