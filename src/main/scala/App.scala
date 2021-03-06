import AllAutos.{AvailableAuto, ListAutos}
import Auction.{Bid, AuctionAuto, ListAuctionAutos}
import argonaut.Argonaut._
import argonaut._
import com.autoservice.storage.{AuctionAutoStatements, AvailableAutoStatements}
import com.twitter.finatra._

class AutoserviceServer extends Controller {

  implicit def AvailableAutoEncodeJson: EncodeJson[AvailableAuto] =
    EncodeJson((p: AvailableAuto) =>
      ("carBrand" := p.carBrand) ->:
        ("logo" := p.logo) ->:
        ("modelsAuction" := Json(
          ("amount" := p.modelsAuction.amount),
          ("list" := p.modelsAuction.list)

        )
          ) ->: jEmptyObject)

  implicit def AuctionAutoEncodeJson: EncodeJson[AuctionAuto] =
    EncodeJson((p: AuctionAuto) =>
      ("carBrand" := p.carBrand) ->:
        ("model" := p.model) ->:
        ("lot" := p.lot) ->:
        ("price" := p.price) ->:
        ("status" := p.status) ->:
        ("exactCloseDate" := p.status) ->:
        ("details" := p.details) ->:
        ("sorted" := p.sorted) ->:
        ("bids" := Json(
          ("id" := p.bids.id),
          ("author" := p.bids.author),
          ("creationTime" := p.bids.creationTime),
          ("price" := p.bids.price)
          )

     ) ->: jEmptyObject)

  val availableAutos = AvailableAutoStatements.getInstance()
  val auctionAutos = AuctionAutoStatements.getInstance()

  get("/api/auction-auto/all-models") { request =>

    val a: ListAutos = availableAutos.readAllAuction
    val json = a.autos.toList.asJson
    render.json(json).toFuture

  }

  get("/api/catalog-auto/all-models") { request =>

    val a: ListAutos = availableAutos.readAllCatalog()
    val json = a.autos.toList.asJson
    render.json(json).toFuture

  }
  get("/api/auction-auto/:carbrand") { request =>

    val carBrand = request.routeParams.getOrElse("carbrand", "default")
    val a: ListAutos = availableAutos.readByCarBrand(carBrand)
    val json = a.autos.toList.asJson
    render.json(json).toFuture

  }


  notFound { request =>
    render.status(404).plain("Not found").toFuture
  }

  class Unauthorized extends Exception

  get("/unauthorized") { request =>
    throw new Unauthorized
  }
  error { request =>
    request.error match {
      case Some(e: ArithmeticException) =>
        render.status(500).plain("whoops, divide by zero!").toFuture
      case Some(e: Unauthorized) =>
        render.status(401).plain("Not Authorized!").toFuture
      case Some(e: UnsupportedMediaType) =>
        render.status(415).plain("Unsupported Media Type!").toFuture
      case _ =>
        render.status(500).plain("Something went wrong!").toFuture
    }
  }


}

object App extends FinatraServer {
  register(new AutoserviceServer())
}
