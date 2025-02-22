package z

// Définir les SPECIFICATIONS clairement -> Avoir une idée CLAIRE du produit finit, avec une très longe liste de spécifications
// -> Nous permet d'implementer!
// (Aussi, Construire les Spécifications Produit est aussi une étape ou il faut travailler et écrire des choses intermédiaire.
// Pour avoir une Liste Claire (idéaliement Structurée à la fin) -> A placée sur Notion aussi. TOUT es sur Notion. Remettre de l'ordre
// dans notion)

// Et pour le software Design -> Le faire en codant le design directement en codant et utilier les 4 types de polymorpshisme quand utile
// (le plus courant étant la généricité -> Fonctions qui fonctionent avec plusieurs type, etc), Variance, etc

// Le succès du Network et de tout tes produit se fera grâce aux software que tu écrit. Et à leur design.
// Car, le Cadre de penser permi par ZIO ou les Classes / instances ont des effets. Ensuite, on voit comment on éxécute ces
// effets, etc. -> Pas forcément de manière concurrente. Permet déja de raisonner avec les OPPOERTUNITES de l'asynchronisme -> Nécéssaire pour
// Tout tes produits, etc.

enum Protocol:
        case Layer7(applicationProtocol: ApplicationProtocol)

enum ApplicationProtocol:
        case HTTP
        case FTP
        case WebSocket
        case gRPC

trait Interface
trait Infrastructure[A, B](verifier: Function[A, B])

// -> Ce qui nous interesse de figurer dans les contrats. (ce sont les elements de base bien sur)
enum Contractualizable:
        case Interface
        case Infrastructure

// -> etc. On ne créer pas encore des instances de Ceux-ci... -> Car on devra les compléter manuellement.
// -> Faire en sorte que l'on puisse créer des instance de ceux-ci! -> Sous certaines conditions, etc.

// utilisé les TypeClasses, etc.
// for Each technologies ->

val pr = Protocol.Layer7(ApplicationProtocol.HTTP)

// pretty big trait.
trait Technology(interfaces: List[Interface]) // -> Publiquement, on dit au developers qu'il ENREGISTRE des TECHNOLOGIES! (car il

// Logique: Chaque fois qu'il y a un nouveau Contrat -> Créer une instance d'un contrat -> Utilisation des implicites et TypeClasses given les
// types, etc! -> Pareil pour le register d'un nouvelle TECHNOLOGY.
// Note: Un contrat, est Construit sur le Type d'une Technologie -> Il faut que le type de la technologie permette de construire le Contrat!
// -> Restreint.
// Et lorsque vous créer une technologie, il faut bien un moyen que les autres

trait Contract

/** Fonction qui permet de scanner une Technologie, -> Et basé la dessus, (sur son interface) Chaque technologie Peut avoir plusieurs interfaces
  * CONTRACTABLE! -> C'est seulement ce QUI NOUS INTERESSE.
  * -> Ce que l'on veut Contractualiser. -> ET h50x offre PLUSIEURS MOYENS!! -> LEverage de l'infrastructure PHYSIQUE AUSSI!
  */

def scanTechnology() = ???
