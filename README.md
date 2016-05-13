# Mini-C-Scanner
:mag: Mini C Scanner (2016 Formal Language Assignment, Dongguk Univ.)

## What is it?
`Mini C` is complete subset of `C Language`
* 7 Keywords
* Support `Block Comment`(eg. /\* ... \*/), `Line Comment` (eg. //...)
* _And more.._

`Mini C Scanner` is [**Lexical Analyzer**](https://en.wikipedia.org/wiki/Lexical_analysis) to generate tokens of source code to use in parser.

## How To Use?
* Input file path of source code by debug arguments _args[0]_
* then, it will **print tokens in console**!

![Example result](http://i.imgur.com/ZwwLAg4.png)

### Example
* Example code are enveloped in `perfect.mc`
```c
  /*
  	A perfect number is an integer which is equal to the sum of all its divisors including 1 but excluding the number itself.
  */

  const int max = 500;

  void main()
  {
  	int i, j, k;
  	int rem, sum; //rem : remainder

  	i = 2;
  	while (i <= max) {
  		sum = 0;
  		k = i / 2;
  		j = 1;
  		while (j <= k) {
  			rem = i % j;
  			if (rem == 0) sum += j;
  			++j;
  		}
  		if (i == sum) write(i);
  		++i;
  	}
  }
```

## TODO
- [ ] Handling Lexical Error
