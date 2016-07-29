import scala.collection.JavaConverters._
import scala.collection.JavaConversions._
import java.io.{InputStream,ByteArrayInputStream}

import com.musicg.wave.Wave
import scalikejdbc._
import java.sql.PreparedStatement
import com.musicg.fingerprint.FingerprintSimilarityComputer



object Database {

  GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings( enabled = false )

  // Class.forName("org.h2.Driver");
  // ConnectionPool.singleton("jdbc:h2:./db/fingerprint", "user", "secret")
  Class.forName("com.mysql.jdbc.Driver")
  ConnectionPool.singleton("jdbc:mysql://localhost:3306/fingerprints", "root", "secret")

  def createDb() {
    DB autoCommit { implicit session =>
      sql"create table fingerprints (filename varchar(100), fingerprint blob)".execute.apply
    }
  }

  def insert(filename: String, fingerprint: Array[Byte]) {
    DB autoCommit { implicit session =>
      val in = new ByteArrayInputStream(fingerprint)
      val bin = ParameterBinder( in, (stmt, idx) => stmt.setBinaryStream(idx, in, fingerprint.length))
      sql"insert into fingerprints values(${filename.split("/").toList.last}, ${bin})".update.apply
    }
  }

  def dump(limit: Int = 10, fingerPrintLimit: Int = 40) {
    DB readOnly { implicit session =>
      sql"select * from fingerprints limit ${limit}".foreach { rs =>
        println(s"${rs.string("filename")} => ${rs.bytes("fingerprint").slice(0,fingerPrintLimit).toList}")
      }
    }
  }

  def compareAll(fingerprint: Array[Byte], compare: (Array[Byte], Array[Byte]) => Float): List[Tuple2[String, Float]] = {
    DB readOnly { implicit session =>
      val fingerprints = sql"select * from fingerprints".map { rs => 
        (rs.string("filename"), rs.bytes("fingerprint"))
      }.list.apply
      fingerprints.map { t => (t._1, compare(t._2, fingerprint)) }
    }
  }
}


object AudioFingerprint {

  def compare(fingerprint1: Array[Byte], fingerprint2: Array[Byte]): Float = {
    val similarityComputer = new FingerprintSimilarityComputer(fingerprint1, fingerprint2)
    similarityComputer.getFingerprintsSimilarity.getSimilarity
  }

  def main(args: Array[String]) {
    
    val db = Database
    
    args(0) match {

      // Create empty DB
      case "-c" => 
        db.createDb

      // Create empty DB
      case "-d" => 
        db.dump(
          if (args.length > 1) args(1).toInt else 10,
          if (args.length > 2) args(2).toInt else 40
        )

      // Insert one or more fingerprints into databse
      case "-i" => {
        args.slice(1, args.length).foreach { filename =>
          val wave =  new Wave(filename)
          db.insert(filename, wave.getFingerprint)
        }
      }

      // Print fingerprint of a file
      case "-f" => {
        val filename = args(1)
        val wave =  new Wave(filename)
        val fingerprint = wave.getFingerprint.toList
        println(s"fingerprint(${filename}[${fingerprint.length}] = ${fingerprint}")
      }

      // Recognize a WAV file which is hopefully a chunk of a sound in the database
      case "-r" => {
        args.slice(1, args.length).foreach { filename =>
          val wave = new Wave(filename)
          val fingerprint = wave.getFingerprint
          val bestMatch = db.compareAll(fingerprint, compare).maxBy(_._2)
          println(s"${bestMatch._1} => ${bestMatch._2}")
        }
      } 

       case _ => println("Wrong paramter")
     }
  }
}

