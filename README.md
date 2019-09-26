# PDFTeacher <En cours de développement>

**Cette application est principalement destinée aux professeurs, elle permet d'éditer et plus précisépent de corriger des copies PDF.**

Elle est basée sur un système d'édition, vous pouvez éditer votre document et sauvegarder l'édition pour reprendre votre travil plus tard. Vous pouvez ensuite exporter le document sous la forme d'un nouveau fichier PDF.
Les éditions sont composés de plusieurs éléments : Les commentaires (Texte), les Notes* et les formes géométriques (Carrés, ronds etc.)

*La somme des notes se calculera automatiquement et elles pouront être exportés dans un tableau CSV.

# Les APIs

J'utilise l'API PDF BOX pour générer des images à partir d'un fichier PDF ainsi que commons-logging et Font BOX qui lui sont nécessaires.
J'ai choisit Gradle pour gérer les dépendances, vous pouvez donc éxécuter ``./gradlew run`` dans un terminal de commande pour exécuter l'application. 

Des releases seront aussi bientôt disponibles.

# L'organisation du code

La classe main se situe dans le package ``fr.themsou.main``

Les classes du package ``fr.themsou.panel`` sont toutes celles qui etendent (extends) JPanel.

On y retrouve donc FooterBar (La barre d'état en bas), MenuBar (Le JMenu en haut), MainScreen (La où s'affichera le document à éditer) et toutes les classes LeftBar<suffixe> qui sont les différents JPanels du JTabbedPane de gauche. Les différents onglets permettrons respectivement de voir la liste des fichiers, ajouter du texte, des notes et des formes géométriques.

Ces différents éléments (texte, notes etc.) ont des classes attribués dans ``fr.themsou.document.editions.elements`` qui etendront de ``Element`` et qui permettront de faire le rendu, de stoquer les donnés d'un élément et d'encoder/décoder les donnés en binaire.

Les éditions seront gérés par ``fr.themsou.document.editions.Edition``, ce fichier permettra de charger les éditions et de stoquer ses donnés. Une classe Document situé dans ``fr.themsou.document.Document`` s'occupera du document en général, elle chargera à l'ouverture du PDF de faire le rendu des pages du PDF sous forme d'Image avec la classe ``fr.themsou.document.render.PDFPagesRender``. ``EditRender`` fera le rendu général des Elements et s'occupera de gérer le système pour déplacer les éléments en stoquant l'élément dans une variable de type ``fr.themsou.utils.Hand``.