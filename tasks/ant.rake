begin
  require 'ant'
  directory 'pkg/classes'
  task :compile => 'pkg/classes' do |t|
    ant.javac :srcdir => 'ext',
              :destdir => t.prerequisites.first,
              :source => '1.5',
              :target => '1.5',
              :debug => true,
              :classpath => '${java.class.path}:${sun.boot.class.path}',
              :includeantRuntime => false
  end

  task :jar => :compile do
    ant.jar :basedir => 'pkg/classes',
            :destfile => 'lib/sand_kit.jar',
            :includes => '*.class'
  end
rescue LoadError
  task :jar do
    puts "Run `jar` with JRuby >= 1.5 to re-compile"
  end
end

# Make sure the jar gets compiled before the gem is built
task :gem => :jar
task :spec => :jar
