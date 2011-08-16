require 'rake/javaextensiontask'

Rake::JavaExtensionTask.new('sandbox') do |ext|
  jruby_home = RbConfig::CONFIG['prefix']
  ext.ext_dir = 'ext/java'
  ext.lib_dir = 'lib/sandbox'
  jars = ["#{jruby_home}/lib/jruby.jar"] + FileList['lib/*.jar']
  ext.classpath = jars.map { |x| File.expand_path(x) }.join(':')
end

task :gem => :compile
task :spec => :compile
