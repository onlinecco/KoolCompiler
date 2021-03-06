package koolc

import utils._
import java.io.File

import lexer._
import ast._
import analyzer._
import code._

object Main {
      var printTokens = false
    var ast = false

  def processOptions(args: Array[String]): Context = {

    val reporter = new Reporter()
    var outDir: Option[File] = None
    var files: List[File] = Nil


    def processOption(args: List[String]): Unit = args match {
      case "-d" :: out :: args =>
        outDir = Some(new File(out))
        processOption(args)

      case "--tokens" :: args =>
        printTokens = true
        processOption(args)

      case "--ast" :: args =>
        ast = true
        processOption(args)

      case f ::args =>
        files = new File(f) :: files
        processOption(args)

      case Nil =>
    }

    processOption(args.toList)

    if (files.size != 1) {
      reporter.fatal("Exactly one file expected, "+files.size+" file(s) given.")
    }

    Context(reporter = reporter, file = files.head, outDir = outDir)
  }


  def main(args: Array[String]) {
    val ctx = processOptions(args)
    if(printTokens){
          val pipeline = Lexer andThen PrintTokens

    val program = pipeline.run(ctx)(ctx.file)

    for (token <- program) { println() }

    }
    else{

    val pipeline = Lexer andThen
                   Parser andThen 
                   NameAnalysis andThen
                   TypeChecking 

    val program = pipeline.run(ctx)(ctx.file)

    println(Printer.apply(program))

    val backend = pipeline andThen CodeGeneration
    backend.run(ctx)(ctx.file)
    }

  }
}
