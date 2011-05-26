JRuby Sandbox
=============

The JRuby sandbox is a reimplementation of _why's freaky freaky sandbox
in JRuby, and is heavily based on [javasand][1] by Ola Bini, but updated
for JRuby 1.6.

## Getting Started

Install JRuby 1.6.1 through RVM first:

    rvm install jruby-1.6.1
    cd jruby_sandbox

The .rvmrc file will create a "jruby_sandbox" gemset and will use
bundler to install the dependencies. To build the JRuby extension, run

    rake compile

This will build a lib/sand_kit.jar file. Once that's done you can load
the library in an irb session like so:

    irb -Ilib -rsandbox

And follow the instructions under "Basic Usage" to play around with it.

## Basic Usage

Sandbox gives you a self-contained JRuby interpreter in which to eval
code without polluting the host environment.

    >> require "sandbox"
    => true
    >> sand = Sandbox.new
    => #<Sandbox:0x455b4492 @kit=#<Sandbox::Kit:0x15353154>>
    >> sand.eval("x = 1 + 2")
    => 3
    >> sand.eval("x")
    => 3
    >> x
    NameError: undefined local variable or method `x' for #<Object:0x74455aa8>

There's also Sandbox#require, which lets you invoke Kernel#require
directly for the sandbox, so you can load any trusted core libraries.
Note that this is a direct binding to Kernel#require, so it will only
load ruby stdlib libraries (i.e. no rubygems support yet).

## TODO

This is very much a work in progress, and there are some areas that need
some attention:

  * Objects are not currently serialized between the sandbox and host
    environments like they are in javasand. Be aware that methods
    defined in the host environment won't be available on sandbox
    objects.

[1]: http://ola-bini.blogspot.com/2006/12/freaky-freaky-sandbox-has-come-to-jruby.html
