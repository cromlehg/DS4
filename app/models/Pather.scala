package models

import play.api.libs.json._

object Segments {

  def apply(points: Array[Array[Double]]): Seq[Segment] =
    for (i <- 1 to points.size - 1)
      yield Segment(points(i - 1)(0), points(i - 1)(1), points(i)(0), points(i)(1))

}

object Segment {

  def apply(startLongitude: Double,
            startLatitude: Double,
            endLongitude: Double,
            endLatitude: Double) =
    new Segment(0, startLongitude,
      startLatitude,
      endLongitude,
      endLatitude)

}

class Segment(val id: Long,
              val startLongitude: Double,
              val startLatitude: Double,
              val endLongitude: Double,
              val endLatitude: Double) {

}

class SegmentVideoContext(val segmentId: Long,
                          val videoId: Long,
                          val startTime: Double,
                          val endTime: Double) {

}

class Path(val id: Long, val descr: String, val segments: Seq[Segment] = Seq()) {

}

class PathSegment(val pathId: Long, val segmentId: Long, order: Int) {

}

object Video {

  implicit val locationWrites = new Writes[Video] {
    def writes(video: Video) = Json.obj(
      "id" -> video.id,
      "pathId" -> video.pathId,
      "link" -> video.link)
  }

}

class Video(val id: Long,
            val pathId: Long,
            val link: String) {

} 