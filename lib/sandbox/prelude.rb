# Alternate "safer" versions of Ruby methods. Mostly non-blocking.
[Fixnum, Bignum, Float].each do |klass|
  klass.class_eval do
    # A very weak version of pow, it doesn't work on Floats, but it's gonna
    # fill the most common uses for now.
    def **(x)
      case x
      when 0; 1
      when 1; self
      else
        y = 1
        while 0 <= (x -= 1) do
          y *= self
        end
        y
      end
    end
  end
end
