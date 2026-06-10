# PLAN DEV

## Sommaire

1. [Phase générique — Livraison via Pull Request GitHub](#phase-générique--livraison-via-pull-request-github)
   - [G0 — Sauvegarder le dépôt local sur ton dépôt GitHub personnel](#étape-g0--sauvegarder-le-dépôt-local-sur-ton-dépôt-github-personnel)
   - [G1 — Connecter le dépôt local au dépôt distant GitHub](#étape-g1--connecter-le-dépôt-local-au-dépôt-distant-github)
   - [G2 — Se placer sur sa branche](#étape-g2--se-placer-sur-sa-branche)
   - [G2b — Créer un point de restauration avant le merge](#étape-g2b--créer-un-point-de-restauration-avant-le-merge)
   - [G3 — Détecter les conflits tôt](#étape-g3--détecter-les-conflits-tôt)
   - [G4 — Résoudre les conflits (si besoin)](#étape-g4--résoudre-les-conflits-si-besoin)
    - [G5 — Valider la résolution (si conflit)](#étape-g5--valider-la-résolution-si-conflit)
    - [G5b — Pousser la branche locale vers le dépôt distant](#étape-g5b--pousser-la-branche-locale-vers-le-dépôt-distant)
    - [G6 — Créer la Pull Request sur GitHub](#étape-g6--créer-la-pull-request-sur-github)
   - [G7 — Review + merge sur GitHub](#étape-g7--review--merge-sur-github)
   - [G7a — Mettre à jour la branche locale develop](#étape-g7a--mettre-à-jour-la-branche-locale-develop)
   - [G7b — Créer un point de restauration après le merge](#étape-g7b--créer-un-point-de-restauration-après-le-merge)
   - [G8 — Déconnecter le dépôt local du dépôt distant](#étape-g8--déconnecter-le-dépôt-local-du-dépôt-distant)
   - [G8b — Sauvegarder le dépôt local sur ton dépôt GitHub personnel (post-merge)](#étape-g8b--sauvegarder-le-dépôt-local-sur-ton-dépôt-github-personnel-post-merge)
   - [G9 — Vérifier l'état du dépôt local](#étape-g9--vérifier-létat-du-dépôt-local)
2. [Phase unique : Création transfert classique](#phase-unique--création-transfert-classique)
   - [Étape 1 — DTOs de requête et réponse (Backend)](#étape-1--dtos-de-requête-et-réponse-backend)
   - [Étape 2 — Converter (Backend)](#étape-2--converter-backend)
   - [Étape 3 — Interface du service (Backend)](#étape-3--interface-du-service-backend)
   - [Étape 4 — Implémentation du service (Backend)](#étape-4--implémentation-du-service-backend)
   - [Étape 5 — Controller REST (Backend)](#étape-5--controller-rest-backend)
   - [Étape 6 — Touchpoints inévitables (Backend)](#étape-6--touchpoints-inévitables-backend)
   - [Étape 7 — Modèle TypeScript (Frontend)](#étape-7--modèle-typescript-frontend)
   - [Étape 8 — Service HTTP (Frontend)](#étape-8--service-http-frontend)
   - [Étape 9 — Composant formulaire (3 étapes)](#étape-9--composant-formulaire-3-étapes)
   - [Étape 10 — Touchpoints inévitables (Frontend)](#étape-10--touchpoints-inévitables-frontend)
   - [Étape 11 — Vérification finale](#étape-11--vérification-finale)
   - [Étape 12 — Livraison via Pull Request GitHub](#étape-12--livraison-via-pull-request-github)
3. [Phase unique : Paiement transfert classique (Retrait)](#phase-unique--paiement-transfert-classique-retrait)
   - [Étape 1 — Ajout méthode repository (Backend)](#étape-1--ajout-méthode-repository-backend)
   - [Étape 2 — Nouveaux DTOs paiement (Backend)](#étape-2--nouveaux-dtos-paiement-backend)
   - [Étape 3 — Converter paiement (Backend)](#étape-3--converter-paiement-backend)
   - [Étape 4 — Interface du service paiement (Backend)](#étape-4--interface-du-service-paiement-backend)
   - [Étape 5 — Implémentation du service paiement (Backend)](#étape-5--implémentation-du-service-paiement-backend)
   - [Étape 6 — Controller paiement (Backend)](#étape-6--controller-paiement-backend)
   - [Étape 7 — Service PDF reçu de paiement (Backend)](#étape-7--service-pdf-reçu-de-paiement-backend)
   - [Étape 8 — Modèle TypeScript paiement (Frontend)](#étape-8--modèle-typescript-paiement-frontend)
   - [Étape 9 — Service HTTP paiement (Frontend)](#étape-9--service-http-paiement-frontend)
   - [Étape 10 — Composant page de paiement (Frontend)](#étape-10--composant-page-de-paiement-frontend)
   - [Étape 11 — Touchpoints inévitables (Frontend)](#étape-11--touchpoints-inévitables-frontend)
    - [Étape 12 — Vérification finale](#étape-12--vérification-finale-1)
    - [Étape 13 — Livraison via Pull Request GitHub](#étape-13--livraison-via-pull-request-github-1)
    - [Récapitulatif des fichiers](#récapitulatif-des-fichiers-1)
 
---

## Contexte

- **Rôle concerné** : AGENT
- **Backend** : Spring MVC 6.1, Hibernate 6.4, Spring Security 6.2, JWT
- **Frontend** : Angular 21 standalone, Tailwind CSS, ngx-translate
- **Branche** : `feat/creation-transfert-classique` (basée sur `develop`)
- **Contrainte stricte** : NE PAS modifier le code backend et frontend existant. Tout le nouveau code va dans des **nouveaux fichiers**. Seules les exceptions inévitables (routing, i18n) sont documentées comme "touchpoints" minimaux.
- **Stratégie d'isolation** : Tous les nouveaux fichiers sont placés dans de nouveaux packages/répertoires qui n'existent pas encore, garantissant zéro conflit de merge avec `develop`.

---

## Décisions validées

| Question | Réponse |
|----------|---------|
| Création des clients | **Automatique** — si CIN inconnu, le client (expéditeur) est créé à la volée |
| Code de retrait | **Alphanumérique 8 car.** (ex: `A7X3K9M2`) |
| Notification | **SMS + Email** via Twilio et Jakarta Mail (existants) |

---

## Phase générique — Livraison via Pull Request GitHub

**Applicable à toute nouvelle fonctionnalité** développée dans une branche locale `feat/nouvelle-fonctionnalite`.

**Concept important :** Pendant le développement, ton dépôt local **n'est pas relié** à GitHub. Tu travailles en isolation. Avant la livraison, tu commences par sauvegarder ta branche sur **ton dépôt GitHub personnel** (backup privé), puis tu connectes temporairement le dépôt de l'équipe pour la PR et le merge. Ensuite tu coupes la liaison.

### Étape G0 — Sauvegarder le dépôt local sur ton dépôt GitHub personnel

Avant toute interaction avec le dépôt de l'équipe, connecte d'abord ton dépôt local à **ton propre dépôt GitHub privé** pour créer une sauvegarde de ta branche de travail.

```bash
# 1. Ajouter ton dépôt distant personnel
git remote add backup https://github.com/smahbblm/PROJET-ATLAS-DEPOT-GITHUB-EQUIPE.git

# 2. Pousser toutes les branches locales sur ton dépôt personnel
git push -u backup feat/nouvelle-fonctionnalite
git push -u backup develop
git push -u backup main

# 3. Déconnecter le dépôt personnel
git remote remove backup
```

> Cette étape garantit que l'intégralité de ton travail (feature + `develop` + `main`) est sauvegardée sur ton propre dépôt privé avant de commencer la phase de livraison sur le dépôt de l'équipe. Tu peux répéter cette étape à tout moment si tu veux une sauvegarde intermédiaire.

### Étape G1 — Connecter le dépôt local au dépôt distant GitHub

```bash
git remote add origin https://github.com/le-depot-de-ton-equipe/le-repo.git
```
Cette commande attache ton dépôt local au dépôt GitHub de l'équipe. Tu n'as à la faire qu'une seule fois par dépôt.

### Étape G2 — Se placer sur sa branche

```bash
git checkout feat/nouvelle-fonctionnalite
```

### Étape G2b — Créer un point de restauration avant le merge

```bash
git tag backup/feat/nouvelle-fonctionnalite/avant-merge
```
> Prend une photo de ta branche feature telle qu'elle est actuellement. Si la fusion ou la résolution de conflit tourne mal, tu peux revenir ici avec `git checkout backup/feat/nouvelle-fonctionnalite/avant-merge`.

### Étape G3 — Détecter les conflits tôt

```bash
git pull origin develop
```
Cette commande télécharge la dernière version de `develop` depuis GitHub et la fusionne dans ta branche locale.

**3 cas possibles :**
- **Aucun conflit** → tu es prêt pour la PR
- **Conflit détecté** → Git affiche un message comme `CONFLICT (content) in fichier.java`. Va à l'étape G4.
- **Erreur "divergent branches"** → utilise `git pull origin develop --no-rebase`

### Étape G4 — Résoudre les conflits (si besoin)

Avant de modifier les fichiers, sauvegarde l'état avant résolution :
```bash
git tag backup/feat/nouvelle-fonctionnalite/avant-resolution-conflit
```

Ouvre le fichier en conflit. Tu verras des marqueurs :
```
<<<<<<< HEAD
ton code modifié
=======
le code venant de develop
>>>>>>> origin/develop
```

**Action :** Supprime les lignes `<<<<<<<`, `=======`, `>>>>>>>`, garde seulement le code correct (le tien, celui de develop, ou un mélange), et enregistre le fichier.

### Étape G5 — Valider la résolution (si conflit)

Si un conflit a été détecté en G3 et résolu en G4, valide la résolution :

```bash
git add .
git commit -m "Resolve merge conflict with develop"
```

### Étape G5b — Pousser la branche locale vers le dépôt distant

**Objectif** : Rendre la branche feature disponible sur le dépôt distant de l'équipe GitHub avant de créer la Pull Request.

- **Cas 1 (conflit résolu en G4–G5)** : la résolution est déjà commitée localement, il reste à pousser.
- **Cas 2 (aucun conflit en G3)** : la branche est déjà propre après le `git pull origin develop`, il faut la pousser.

```bash
git push origin feat/nouvelle-fonctionnalite
```

> Cette étape est **obligatoire** même en l'absence de conflit — GitHub a besoin de la branche pour pouvoir créer la Pull Request.

### Étape G6 — Créer la Pull Request sur GitHub

1. Va sur le dépôt GitHub
2. Clique sur **"Pull requests" → "New pull request"**
3. Sélectionne **base : `develop`** et **compare : `feat/nouvelle-fonctionnalite`**
4. Ajoute un titre et une description claire
5. Clique **"Create pull request"**

### Étape G7 — Review + merge sur GitHub

- Relis le code dans l'onglet **"Files changed"** sur GitHub
- Une fois approuvé, clique **"Merge pull request"**

### Étape G7a — Mettre à jour la branche locale `develop`

Après le merge sur GitHub, la branche locale `develop` est en retard. Mets-la à jour depuis le dépôt distant de l'équipe :

```bash
git checkout develop
git pull origin develop
```

> Cette étape est indispensable avant de créer un point de restauration, afin que le tag capture l'état final après merge.

### Étape G7b — Créer un point de restauration après le merge

```bash
git tag backup/feat/nouvelle-fonctionnalite/apres-merge
```
> Sauvegarde l'état final après le merge. Si un problème est découvert plus tard, tu peux revenir sur ce point.

### Comment restaurer un point de restauration ?

```bash
# Voir la liste de tous les tags de backup
git tag | grep backup/

# Revenir à un état sauvegardé
git checkout backup/feat/nouvelle-fonctionnalite/avant-merge
# Ou créer une nouvelle branche à partir du tag
git checkout -b feat/nouvelle-fonctionnalite-restore backup/feat/nouvelle-fonctionnalite/avant-merge
```

### Étape G8 — Déconnecter le dépôt local du dépôt distant

```bash
git remote remove origin
```
Cette commande coupe la liaison entre ton dépôt local et GitHub. Ton dépôt redevient **100% local et indépendant**. Tu peux attaquer la prochaine fonctionnalité en toute isolation.

### Étape G8b — Sauvegarder le dépôt local sur ton dépôt GitHub personnel (post-merge)

Après la déconnexion du dépôt de l'équipe, sauvegarde l'état final sur **ton dépôt GitHub privé** avant de poursuivre.

```bash
# 1. Ajouter ton dépôt distant personnel
git remote add backup https://github.com/smahbblm/PROJET-ATLAS-DEPOT-GITHUB-EQUIPE.git

# 2. Pousser la branche develop (après merge) et la branche feature
git push -u backup develop
git push -u backup feat/nouvelle-fonctionnalite

# 3. Déconnecter le dépôt personnel
git remote remove backup
```

> Cette étape est le symétrique de G0 : elle sauvegarde l'état final après le merge, avec `develop` à jour et la branche feature dans son dernier état. Tu peux aussi l'utiliser comme backup intermédiaire entre deux fonctionnalités.

### Étape G9 — Vérifier l'état du dépôt local

**Objectif** : S'assurer que le dépôt local est bien déconnecté de tout distant et connaître la branche active avant d'attaquer une nouvelle fonctionnalité.

```bash
# Voir les remotes distants (aucun = isolation)
git remote -v

# Voir la branche active
git branch --show-current
```

- Si `git remote -v` n'affiche **rien** → le dépôt est bien en isolation.
- Si `git remote -v` affiche une ou plusieurs lignes → un remote est encore connecté. Vérifie que ce n'est pas gênant pour la suite.

---

## Phase unique : Création transfert classique

### Étape 1 — DTOs de requête et réponse (Backend)

**Objectif** : Définir les structures de données pour l'API de création de transfert.

**Fichiers à créer** (nouveaux) :
- `src/main/java/com/okane/dto/requestDto/TransfertRequestDTO.java`
  - Inner class `ExpediteurInfo` : `nom`, `prenom`, `numPieceIdentite`, `telephone`, `paysId`, `email`
  - Inner class `BeneficiaireInfo` : `nom`, `prenom`, `telephone`, `paysId`
  - Champs racine : `expediteur` (ExpediteurInfo), `beneficiaire` (BeneficiaireInfo), `montantEnvoye` (BigDecimal), `corridorId` (Long), `deviceSourceId` (Long), `deviceDestinationId` (Long)
  - Annotations Jakarta Validation : `@NotNull`, `@NotBlank`, `@Positive`, `@Valid` sur les inner objects
- `src/main/java/com/okane/dto/responseDto/TransfertResponseDTO.java`
  - Champs : `id`, `codeRetrait`, `montantEnvoye`, `frais`, `montantNet`, `statut`, `expediteurNom`, `expediteurPrenom`, `beneficiaireNom`, `beneficiairePrenom`, `dateCreation`

---

### Étape 2 — Converter (Backend)

**Objectif** : Assurer la conversion entre l'entité `Transfert` et `TransfertResponseDTO`.

**Fichier à créer** (nouveau) :
- `src/main/java/com/okane/dto/converter/TransfertConverter.java`
  - Méthode `toResponseDto(Transfert entity)` : mappe tous les champs de l'entité vers le DTO de réponse
  - Pas de méthode inverse (le `TransfertRequestDTO` est trop différent de l'entité pour une conversion directe)

---

### Étape 3 — Interface du service (Backend)

**Objectif** : Définir le contrat du service de création de transfert.

**Fichier à créer** (nouveau) :
- `src/main/java/com/okane/service/TransfertService.java`
  - Méthode unique : `TransfertResponseDTO creerTransfert(TransfertRequestDTO request, String agentEmail)`
  - `agentEmail` est extrait du JWT (passe-plat depuis le controller)

---

### Étape 4 — Implémentation du service (Backend)

**Objectif** : Toute la logique métier de création d'un transfert classique.

**Fichier à créer** (nouveau) :
- `src/main/java/com/okane/service/impl/TransfertServiceImpl.java`
  - Logique complète :
    1. Résoudre l'agent connecté via `UserRepository.findByEmail(agentEmail)` — vérifier `ROLE_AGENT`
    2. **Find-or-create** le `Client` expéditeur via `numPieceIdentite` :
       - Si `clientRepository.existsByNumPieceIdentite(...)` → charger existant
       - Sinon → créer nouveau Client avec nom, prenom, telephone, pays, email
    3. Charger le `Corridor` via `corridorRepository.findById(corridorId)`
    4. Charger la `GrilleTarifaire` active pour ce corridor et vérifier `plafondMin <= montant <= plafondMax`
    5. Calculer les frais : `frais = grille.fraisFixe + (montant * grille.fraisPercentage / 100)`
    6. Calculer `montantNet = montantEnvoye - frais`
    7. Générer code retrait : 8 car. alphanumériques (excluant O/0/I/1), boucle jusqu'à unicité via `transfertRepository.existsByCodeRetrait()`
    8. Créer et persister `Transfert` avec statut `EN_ATTENTE`
    9. **Notification** : Appeler `NotificationServiceImpl.sendTransferNotification()` — déclenche Email + SMS via les services existants
    10. Retourner `TransfertResponseDTO` via `TransfertConverter`

  - Injections : `TransfertRepository`, `ClientRepository`, `UserRepository`, `CorridorRepository`, `GrilleTarifaireRepository`, `DeviseRepository`, `TransfertConverter`, `NotificationService`

---

### Étape 5 — Controller REST (Backend)

**Objectif** : Exposer l'endpoint API pour les agents.

**Fichier à créer** (nouveau) :
- `src/main/java/com/okane/controller/TransfertController.java`
  - Endpoint : `POST /api/v1/agent/transfers`
  - Sécurité : `@PreAuthorize("hasRole('AGENT')")`
  - Injecte `TransfertService`
  - Extrait l'email de l'agent depuis `SecurityContextHolder.getContext().getAuthentication().getName()`
  - Valide le body avec `@Valid`
  - Retourne `ResponseEntity<TransfertResponseDTO>` avec statut `201 CREATED`

---

### Étape 6 — Touchpoints inévitables (Backend)

**Objectif** : Corrections minimales dans les fichiers existants pour que le code compile.

**Fichier modifié** : `src/main/java/com/okane/repository/TransfertRepository.java`
- Changement unique : `JpaRepository<Transfert, UUID>` → `JpaRepository<Transfert, Long>`
- Ajout d'une méthode : `boolean existsByCodeRetrait(String codeRetrait)`
- Justification : Bug existant (ID déclaré `Long` dans l'entité mais `UUID` dans le repository). Sans cette correction, le projet ne compile pas.

**Fichiers NON modifiés** (sécurité déjà configurable) :
- `SecurityConfig.java` : Aucun changement nécessaire — `@PreAuthorize` suffit
- `Transfert.java` : Aucun changement — l'entité a déjà les champs nécessaires (agent, client, devises, expediteur*, beneficiaire*, etc.)

---

### Étape 7 — Modèle TypeScript (Frontend)

**Objectif** : Définir les interfaces TypeScript correspondant aux DTOs backend.

**Fichier à créer** (nouveau) :
- `src/app/pages/agent/transfer-creation/models/transfer.model.ts`
  - Interface `ExpediteurInfo` : `nom`, `prenom`, `numPieceIdentite`, `telephone`, `paysId`, `email`
  - Interface `BeneficiaireInfo` : `nom`, `prenom`, `telephone`, `paysId`
  - Interface `TransfertRequest` : `expediteur`, `beneficiaire`, `montantEnvoye`, `corridorId`, `deviceSourceId`, `deviceDestinationId`
  - Interface `TransfertResponse` : `id`, `codeRetrait`, `montantEnvoye`, `frais`, `montantNet`, `statut`, `expediteurNom`, `expediteurPrenom`, `beneficiaireNom`, `beneficiairePrenom`, `dateCreation`

---

### Étape 8 — Service HTTP (Frontend)

**Objectif** : Appeler l'API backend depuis le frontend.

**Fichier à créer** (nouveau) :
- `src/app/pages/agent/transfer-creation/services/transfer.service.ts`
  - Méthode : `creerTransfert(request: TransfertRequest): Observable<TransfertResponse>`
  - Appel : `POST /api/v1/agent/transfers` (proxy Angular vers backend 8081)
  - Gestion des erreurs avec `catchError`

---

### Étape 9 — Composant formulaire (3 étapes)

**Objectif** : Interface utilisateur pour la création de transfert.

**Fichiers à créer** (nouveaux) :
- `src/app/pages/agent/transfer-creation/transfer-creation.component.ts`
  - Composant standalone avec `CommonModule`, `FormsModule`/`ReactiveFormsModule`
  - Gère 3 étapes via un `currentStep: number` (1, 2, 3)
  - Appelle `TransferService.creerTransfert()` à l'étape 3
  - Affiche le code retrait + message notification en cas de succès
- `src/app/pages/agent/transfer-creation/transfer-creation.component.html`
  - **Étape 1** : Formulaire expéditeur (nom, prénom, CIN, téléphone, pays, email)
  - **Étape 2** : Formulaire bénéficiaire (nom, prénom, téléphone, pays, devise destination)
  - **Étape 3** : Montant + récapitulatif + bouton confirmer
  - Barre de progression des étapes
- `src/app/pages/agent/transfer-creation/transfer-creation.component.css`
  - Styles Tailwind

---

### Étape 10 — Touchpoints inévitables (Frontend)

**Objectif** : Brancher le nouveau composant dans le routage et les traductions.

**Fichier modifié** : `src/app/app.routes.ts`
- Ajout d'une route enfant dans le bloc agent :
  ```typescript
  {
    path: 'transfers/new',
    loadComponent: () => import('./pages/agent/transfer-creation/transfer-creation.component')
      .then(m => m.TransferCreationComponent)
  }
  ```
- Justification : Impossible d'enregistrer une route Angular sans modifier `app.routes.ts`.

**Fichiers modifiés** : Fichiers i18n
- `src/assets/i18n/fr.json` : Clés pour le formulaire de transfert
- `src/assets/i18n/en.json` : Traductions anglaises
- `src/assets/i18n/ar.json` : Traductions arabes
- Clés à ajouter : titres étapes, labels champs, erreurs validation, message succès (code retrait + notification SMS/Email)
- Justification : Les traductions doivent exister dans les fichiers de langue existants.

---

### Étape 11 — Vérification finale

**Objectif** : Valider que le build passe et que le flux fonctionne.

- Backend : `mvn compile` (vérifier que le nouveau code compile avec le fix `TransfertRepository`)
- Frontend : `ng build` (vérifier que le nouveau composant et la route marchent)
- Test manuel : `POST /api/v1/agent/transfers` avec un token JWT d'agent
- Vérifier que les notifications SMS et Email sont bien déclenchées

---

### Récapitulatif des fichiers

#### Nouveaux fichiers (aucun conflit de merge)

```
# Backend (6 nouveaux fichiers)
back/src/main/java/com/okane/dto/requestDto/TransfertRequestDTO.java
back/src/main/java/com/okane/dto/responseDto/TransfertResponseDTO.java
back/src/main/java/com/okane/dto/converter/TransfertConverter.java
back/src/main/java/com/okane/service/TransfertService.java
back/src/main/java/com/okane/service/impl/TransfertServiceImpl.java
back/src/main/java/com/okane/controller/TransfertController.java

# Frontend (5 nouveaux fichiers)
front/src/app/pages/agent/transfer-creation/transfer-creation.component.ts
front/src/app/pages/agent/transfer-creation/transfer-creation.component.html
front/src/app/pages/agent/transfer-creation/transfer-creation.component.css
front/src/app/pages/agent/transfer-creation/services/transfer.service.ts
front/src/app/pages/agent/transfer-creation/models/transfer.model.ts
```

#### Touchpoints (fichiers existants modifiés)

| Fichier | Modification | Risque conflit |
|---------|-------------|----------------|
| `TransfertRepository.java` | Fix `UUID`→`Long` + ajout `existsByCodeRetrait()` | **Élevé** |
| `app.routes.ts` | Ajout route `/agent/transfers/new` | Faible |
| Fichiers i18n (`fr.json`, `en.json`, `ar.json`) | Ajout clés formulaire transfert | Très faible |

---

### Étape 12 — Livraison via Pull Request GitHub

**Objectif** : Livrer la branche `feat/creation-transfert-classique` sur le dépôt de l'équipe `https://github.com/AitSaad0/Okane-Transfer` via Pull Request, avec le compte `smahbblm`.

Cette étape applique la **Phase générique** (étapes G0 à G8 ci-dessus) avec les paramètres concrets de cette feature.

```bash
# ── Configurer le compte git pour toutes les commandes suivantes ──
alias git='git -c user.name=smahbblm -c user.email=190425159+smahbblm@users.noreply.github.com'
```

> L'alias ci-dessus est optionnel mais pratique. Tu peux aussi préfixer chaque commande avec `-c user.name=smahbblm -c user.email=190425159+smahbblm@users.noreply.github.com`.

| Étape générique | Commande adaptée |
|-----------------|------------------|
| **G0** — Backup perso | `git remote add backup https://github.com/smahbblm/PROJET-ATLAS-DEPOT-GITHUB-EQUIPE.git`<br>`git push -u backup feat/creation-transfert-classique`<br>`git push -u backup develop`<br>`git push -u backup main`<br>`git remote remove backup` |
| **G1** — Connecter l'équipe | `git remote add origin https://github.com/AitSaad0/Okane-Transfer.git` |
| **G2** — Se placer sur la branche | `git checkout feat/creation-transfert-classique` |
| **G2b** — Tag avant merge | `git tag backup/feat/creation-transfert-classique/avant-merge` |
| **G3** — Pull de develop | `git pull origin develop` |
| **G4** — Résoudre conflits (si besoin) | Ouvrir les fichiers en conflit, supprimer les marqueurs, garder le code correct |
| **G5** — Valider (si conflit) | `git add . && git commit -m "Resolve merge conflict with develop"` |
| **G5b** — Pousser la branche | `git push origin feat/creation-transfert-classique` |
| **G6** — Créer la PR | Aller sur `https://github.com/AitSaad0/Okane-Transfer/pulls` → **New pull request** → base: `develop` / compare: `feat/creation-transfert-classique` |
| **G7** — Review + merge | Cliquer **"Merge pull request"** sur GitHub |
| **G7a** — Mettre à jour develop local | `git checkout develop`<br>`git pull origin develop` |
| **G7b** — Tag après merge | `git tag backup/feat/creation-transfert-classique/apres-merge` |
| **G8** — Déconnecter | `git remote remove origin` |
| **G8b** — Backup perso (post-merge) | `git remote add backup https://github.com/smahbblm/PROJET-ATLAS-DEPOT-GITHUB-EQUIPE.git`<br>`git push -u backup develop`<br>`git push -u backup feat/creation-transfert-classique`<br>`git remote remove backup` |
| **G9** — Vérifier l'état du dépôt | `git remote -v`<br>`git branch --show-current` |

**Rappel** : Toutes les commandes `git` doivent être exécutées avec le compte `smahbblm`. Si tu n'utilises pas l'alias, chaque commande doit être préfixée ainsi :
```bash
git -c user.name=smahbblm -c user.email=190425159+smahbblm@users.noreply.github.com push origin feat/creation-transfert-classique
```

---

## Phase unique : Paiement transfert classique (Retrait)

### Contexte

- **Rôle concerné** : AGENT (agence de destination)
- **Branche** : `feat/paiement-transfert-classique` (basée sur `develop`)
- **Dépend sur** : La branche `feat/creation-transfert-classique` déjà mergée dans `develop`
- **Contrainte stricte** : NE PAS modifier le code backend et frontend existant. Tout le nouveau code va dans des **nouveaux fichiers**. Seules les exceptions inévitables (routing, sidebar, i18n, repository) sont documentées comme "touchpoints" minimaux.
- **Stratégie d'isolation** : Tous les nouveaux fichiers sont placés dans de nouveaux packages/répertoires qui n'existent pas encore, garantissant zéro conflit de merge avec `develop`.

---

### Décisions d'architecture

| Question | Réponse |
|----------|---------|
| Recherche du transfert | Par **code retrait** OU **numéro téléphone bénéficiaire** — deux endpoints distincts |
| Vérification identité | Saisie obligatoire du numéro de pièce d'identité du bénéficiaire avant validation |
| Statut après paiement | `EN_ATTENTE` → `PAYE` |
| Reçu PDF | Nouveau endpoint spécifique au reçu de paiement (s'inspire de `RecuPdfServiceImpl`) |
| API design | **Phase 1** : Recherche → **Phase 2** : Vérification identité + Confirmation paiement |
| Notification | `TypeNotification.CONFIRMATION_RETRAIT` (existe déjà) |
| DTO réponse | Nouveau `PaiementResponseDTO` dédié avec tous les champs du récap détaillé |

---

### Étape 1 — Ajout méthode repository (Backend)

**Objectif** : Permettre la recherche d'un transfert par code retrait ou téléphone bénéficiaire.

**Fichier modifié** : `src/main/java/com/okane/repository/TransfertRepository.java`
- Ajout de deux méthodes (touchpoint mineur, additif sans conflit) :
  ```java
  Optional<Transfert> findByCodeRetrait(String codeRetrait);
  
  @Query("SELECT t FROM Transfert t JOIN FETCH t.beneficiaire b WHERE b.telephone = :telephone")
  List<Transfert> findByBeneficiaireTelephone(@Param("telephone") String telephone);
  ```

---

### Étape 2 — Nouveaux DTOs paiement (Backend)

**Objectif** : Définir les structures de données pour la recherche et le paiement.

**Fichiers à créer** (nouveaux) :
- `src/main/java/com/okane/dto/requestDto/RechercheTransfertDTO.java`
  - Champs optionnels : `codeRetrait` (String), `telephoneBeneficiaire` (String)
  - Au moins un des deux doit être renseigné
- `src/main/java/com/okane/dto/requestDto/PaiementRequestDTO.java`
  - Champs : `transfertId` (Long), `pieceIdentiteBeneficiaire` (String, @NotBlank), `codeRetrait` (String, @NotBlank)
- `src/main/java/com/okane/dto/responseDto/PaiementResponseDTO.java`
  - Champs détaillés pour l'affichage du récap :
    - **Infos générales** : `id`, `codeRetrait`, `reference`, `corridorDescription`, `dateEnvoi`, `statut`
    - **Expéditeur** : `expediteurNomComplet`, `expediteurTelephone`, `expediteurEmail`, `expediteurPieceIdentite`, `expediteurPays`
    - **Bénéficiaire** : `beneficiaireNomComplet`, `beneficiaireTelephone`, `beneficiairePays`
    - **Frais** : `montantDepart`, `fraisFixes`, `fraisProportionnels`, `totalFrais`, `montantNet`
    - **Taux de change** : `tauxChange`, `sourceTaux`
    - **Montant reçu** : `montantRecu`, `deviseDestination`
    - **Dates** : `dateCreation`, `datePaiement`
    - **Agents** : `agentEnvoiNom`, `agentEnvoiPrenom`, `agentPaiementNom`, `agentPaiementPrenom`
    - `paye` (boolean) — indique si le paiement a déjà été effectué

---

### Étape 3 — Converter paiement (Backend)

**Objectif** : Convertir l'entité `Transfert` vers `PaiementResponseDTO` avec tous les champs détaillés.

**Fichier à créer** (nouveau) :
- `src/main/java/com/okane/dto/converter/PaiementConverter.java`
  - Méthode `toPaiementResponseDTO(Transfert t)` : mappe tous les champs détaillés
  - Récupère le taux de change, les frais fixes/proportionnels, les pays via l'entité `Transfert` et ses relations

---

### Étape 4 — Interface du service paiement (Backend)

**Objectif** : Définir le contrat du service de paiement.

**Fichier à créer** (nouveau) :
- `src/main/java/com/okane/service/PaiementService.java`
  - Méthodes :
    - `PaiementResponseDTO rechercherParCodeRetrait(String codeRetrait)`
    - `PaiementResponseDTO rechercherParTelephoneBeneficiaire(String telephone)`
    - `PaiementResponseDTO payerTransfert(PaiementRequestDTO request, String agentEmail)`

---

### Étape 5 — Implémentation du service paiement (Backend)

**Objectif** : Logique métier complète du paiement.

**Fichier à créer** (nouveau) :
- `src/main/java/com/okane/service/impl/PaiementServiceImpl.java`
  1. `rechercherParCodeRetrait(String codeRetrait)` :
     - Appel `transfertRepository.findByCodeRetrait(codeRetrait)`
     - Vérifier que le transfert existe → sinon `ResourceNotFoundException`
     - Retourner `PaiementResponseDTO` via `PaiementConverter`
  2. `rechercherParTelephoneBeneficiaire(String telephone)` :
     - Appel `transfertRepository.findByBeneficiaireTelephone(telephone)`
     - Filtrer les transferts `EN_ATTENTE`
     - Si un seul trouvé → retourner son DTO
     - Si plusieurs → lever une exception demandant d'utiliser le code retrait
     - Si aucun → erreur
  3. `payerTransfert(PaiementRequestDTO request, String agentEmail)` :
     - Charger le transfert via `transfertRepository.findById(request.getTransfertId())`
     - Vérifier que `statut == EN_ATTENTE` → sinon erreur
     - Vérifier que `codeRetrait` correspond
     - Vérifier que `pieceIdentiteBeneficiaire` correspond à celle en base (du bénéficiaire du transfert)
     - Résoudre l'agent connecté via `userRepository.findByEmail(agentEmail)` — vérifier `ROLE_AGENT`
     - Résoudre l'agence de l'agent via `agenceRepository.findByUsers_Id(agent.getId())`
     - Mettre à jour le transfert :
       - `statut = StatutTransfert.PAYE`
       - `datePaiement = LocalDateTime.now()`
       - `agentPaiement = agent`
       - `agencePaiement = agence`
     - Sauvegarder le transfert
     - Envoyer notification `CONFIRMATION_RETRAIT` via `NotificationService`
     - Retourner `PaiementResponseDTO` mis à jour

  - Injections : `TransfertRepository`, `UserRepository`, `AgenceRepository`, `PaiementConverter`, `NotificationService`

---

### Étape 6 — Controller paiement (Backend)

**Objectif** : Exposer les endpoints API pour le paiement.

**Fichier à créer** (nouveau) :
- `src/main/java/com/okane/controller/PaiementController.java`
  - Base path : `@RequestMapping("/api/v1/agent/transfers")`
  - Sécurité : `@PreAuthorize("hasRole('AGENT')")` sur toutes les méthodes
  - Endpoints :
    - `GET /api/v1/agent/transfers/search/code?codeRetrait=XXX` → recherche par code
    - `GET /api/v1/agent/transfers/search/telephone?telephone=YYY` → recherche par téléphone
    - `POST /api/v1/agent/transfers/{id}/payer` → confirmer le paiement
  - Injecte `PaiementService`

---

### Étape 7 — Service PDF reçu de paiement (Backend)

**Objectif** : Générer un reçu PDF spécifique au paiement (statut PAYÉ, date de paiement, agent payeur).

**Fichier à créer** (nouveau) :
- `src/main/java/com/okane/service/impl/RecuPaiementPdfServiceImpl.java`
  - Implémente `RecuPdfService` (interface existante réutilisée)
  - Méthode `genererRecu(Transfert t)` : s'inspire de `RecuPdfServiceImpl` mais adaptée au contexte paiement
  - Ajoute les informations de paiement : date de paiement, agent payeur, agence de paiement
  - Mentionne "REÇU DE PAIEMENT" au lieu de "REÇU DE TRANSFERT"
  - Réutilise `PdfGenerator` existant

**Fichier modifié** (touchpoint) : `RecuPdfController.java` ou nouveau endpoint
- Option : Ajouter un endpoint `GET /api/v1/agent/transfers/{id}/recu-paiement` qui utilise le nouveau service
- Ou modifier le `RecuPdfServiceImpl` existant pour détecter le statut et adapter le template

---

### Étape 8 — Modèle TypeScript paiement (Frontend)

**Objectif** : Définir les interfaces TypeScript pour le paiement.

**Fichier à créer** (nouveau) :
- `src/app/pages/agent/transfer-payment/models/payment.model.ts`
  - Interface `RechercheTransfertRequest` : `codeRetrait?`, `telephoneBeneficiaire?`
  - Interface `PaiementRequest` : `transfertId`, `pieceIdentiteBeneficiaire`, `codeRetrait`
  - Interface `PaiementResponse` : tous les champs du `PaiementResponseDTO`

---

### Étape 9 — Service HTTP paiement (Frontend)

**Objectif** : Appeler l'API backend depuis le frontend.

**Fichier à créer** (nouveau) :
- `src/app/pages/agent/transfer-payment/services/payment.service.ts`
  - Méthodes :
    - `searchByCode(codeRetrait: string): Observable<PaiementResponse>`
    - `searchByTelephone(telephone: string): Observable<PaiementResponse>`
    - `confirmPayment(request: PaiementRequest): Observable<PaiementResponse>`
    - `downloadPaymentReceipt(transfertId: number): Observable<Blob>`
  - Gestion des erreurs avec `catchError`

---

### Étape 10 — Composant page de paiement (Frontend)

**Objectif** : Interface utilisateur pour la recherche et le paiement d'un transfert.

**Fichiers à créer** (nouveaux) :
- `src/app/pages/agent/transfer-payment/transfer-payment.component.ts`
  - Composant standalone avec `CommonModule`, `ReactiveFormsModule`
  - États :
    1. **Recherche** : champ de saisie (code retrait OU téléphone) + bouton rechercher
    2. **Récapitulatif** (si transfert trouvé) : affiche tous les détails du transfert :
       - Infos générales (Référence, Corridor, Date d'envoi, Code retrait, Statut)
       - Expéditeur (Nom complet, Téléphone, Email, Pièce identité, Pays)
       - Bénéficiaire (Nom complet, Téléphone, Pays)
       - Détail des frais (Montant départ, Frais fixes, Total frais, Montant net)
       - Taux de change (Taux appliqué, Source du taux)
       - Montant reçu par le bénéficiaire
    3. **Validation** (sous le récap) : champ obligatoire pièce d'identité bénéficiaire + bouton "Valider le paiement"
    4. **Succès** : confirmation + bouton télécharger reçu PDF + bouton nouvelle recherche
  - Gère les erreurs (code invalide, transfert déjà payé, etc.)

- `src/app/pages/agent/transfer-payment/transfer-payment.component.html`
  - Template avec affichage conditionnel selon l'état
  - Design Tailwind cohérent avec le reste de l'application
  - Deux options de recherche (code retrait ou téléphone) via un sélecteur ou deux champs distincts
  - Section récapitulative structurée en blocs visuels
  - Section validation avec champ obligatoire

- `src/app/pages/agent/transfer-payment/transfer-payment.component.css`
  - Styles Tailwind

---

### Étape 11 — Touchpoints inévitables (Frontend)

**Objectif** : Brancher le nouveau composant dans le routage, le sidebar et les traductions.

**Fichier modifié** : `src/app/app.routes.ts`
- Ajout d'une route enfant dans le bloc agent :
  ```typescript
  {
    path: 'transfers/payment',
    loadComponent: () => import('./pages/agent/transfer-payment/transfer-payment.component')
      .then(m => m.TransferPaymentComponent)
  }
  ```

**Fichier modifié** : `src/app/pages/shared/sidebar/sidebar.component.ts`
- Ajout d'une entrée de menu pour le paiement dans le tableau `navItems` (rôle `AGENT`) :
  ```typescript
  { label: 'WITHDRAWAL', icon: '\u{1F4B0}', route: '/agent/transfers/payment', roles: ['AGENT'] },
  ```
- La clé `WITHDRAWAL` sera traduite via i18n

**Fichier modifié** : `src/app/pages/shared/sidebar/sidebar.component.html`
- (si nécessaire) Vérifier que le nouveau menu s'affiche correctement

**Fichiers modifiés** : Fichiers i18n
- `src/assets/i18n/fr.json` : Clés pour la page de paiement
- `src/assets/i18n/en.json` : Traductions anglaises
- `src/assets/i18n/ar.json` : Traductions arabes
- Clés à ajouter : `WITHDRAWAL`, `SEARCH_BY_CODE`, `SEARCH_BY_PHONE`, `TRANSFER_DETAILS`, `SENDER_INFO`, `BENEFICIARY_INFO`, `FEES_DETAILS`, `EXCHANGE_RATE`, `AMOUNT_RECEIVED`, `VALIDATE_PAYMENT`, `IDENTITY_DOCUMENT`, `PAYMENT_SUCCESS`, `DOWNLOAD_PAYMENT_RECEIPT`, `NEW_SEARCH`, `TRANSFER_NOT_FOUND`, `ALREADY_PAID`, `IDENTITY_MISMATCH`, etc.

---

### Étape 12 — Vérification finale

**Objectif** : Valider que le build passe et que le flux fonctionne.

- Backend : `mvn compile` (vérifier que le nouveau code compile)
- Frontend : `ng build` (vérifier que le nouveau composant et la route marchent)
- Test manuel :
  1. Créer un transfert via `POST /api/v1/agent/transfers`
  2. Chercher par code retrait via `GET /api/v1/agent/transfers/search/code?codeRetrait=XXX`
  3. Chercher par téléphone bénéficiaire via `GET /api/v1/agent/transfers/search/telephone?telephone=YYY`
  4. Payer via `POST /api/v1/agent/transfers/{id}/payer`
  5. Vérifier que le statut est passé à `PAYE`
  6. Télécharger le reçu de paiement PDF
- Vérifier que la notification `CONFIRMATION_RETRAIT` est bien déclenchée

---

### Étape 13 — Livraison via Pull Request GitHub

**Objectif** : Livrer la branche `feat/paiement-transfert-classique` sur le dépôt de l'équipe `https://github.com/AitSaad0/Okane-Transfer` via Pull Request, avec le compte `smahbblm`.

Cette étape applique la **Phase générique** (étapes G0 à G8 ci-dessus) avec les paramètres concrets de cette feature.

```bash
# ── Configurer le compte git pour toutes les commandes suivantes ──
alias git='git -c user.name=smahbblm -c user.email=190425159+smahbblm@users.noreply.github.com'
```

> L'alias ci-dessus est optionnel mais pratique. Tu peux aussi préfixer chaque commande avec `-c user.name=smahbblm -c user.email=190425159+smahbblm@users.noreply.github.com`.

| Étape générique | Commande adaptée |
|-----------------|------------------|
| **G0** — Backup perso | `git remote add backup https://github.com/smahbblm/PROJET-ATLAS-DEPOT-GITHUB-EQUIPE.git`<br>`git push -u backup feat/paiement-transfert-classique`<br>`git push -u backup develop`<br>`git push -u backup main`<br>`git remote remove backup` |
| **G1** — Connecter l'équipe | `git remote add origin https://github.com/AitSaad0/Okane-Transfer.git` |
| **G2** — Se placer sur la branche | `git checkout feat/paiement-transfert-classique` |
| **G2b** — Tag avant merge | `git tag backup/feat/paiement-transfert-classique/avant-merge` |
| **G3** — Pull de develop | `git pull origin develop` |
| **G4** — Résoudre conflits (si besoin) | Ouvrir les fichiers en conflit, supprimer les marqueurs, garder le code correct |
| **G5** — Valider (si conflit) | `git add . && git commit -m "Resolve merge conflict with develop"` |
| **G5b** — Pousser la branche | `git push origin feat/paiement-transfert-classique` |
| **G6** — Créer la PR | Aller sur `https://github.com/AitSaad0/Okane-Transfer/pulls` → **New pull request** → base: `develop` / compare: `feat/paiement-transfert-classique` |
| **G7** — Review + merge | Cliquer **"Merge pull request"** sur GitHub |
| **G7a** — Mettre à jour develop local | `git checkout develop`<br>`git pull origin develop` |
| **G7b** — Tag après merge | `git tag backup/feat/paiement-transfert-classique/apres-merge` |
| **G8** — Déconnecter | `git remote remove origin` |
| **G8b** — Backup perso (post-merge) | `git remote add backup https://github.com/smahbblm/PROJET-ATLAS-DEPOT-GITHUB-EQUIPE.git`<br>`git push -u backup develop`<br>`git push -u backup feat/paiement-transfert-classique`<br>`git remote remove backup` |
| **G9** — Vérifier l'état du dépôt | `git remote -v`<br>`git branch --show-current` |

**Rappel** : Toutes les commandes `git` doivent être exécutées avec le compte `smahbblm`. Si tu n'utilises pas l'alias, chaque commande doit être préfixée ainsi :
```bash
git -c user.name=smahbblm -c user.email=190425159+smahbblm@users.noreply.github.com push origin feat/paiement-transfert-classique
```

---

### Récapitulatif des fichiers

#### Nouveaux fichiers (aucun conflit de merge)

```
# Backend (8 nouveaux fichiers)
back/src/main/java/com/okane/dto/requestDto/RechercheTransfertDTO.java
back/src/main/java/com/okane/dto/requestDto/PaiementRequestDTO.java
back/src/main/java/com/okane/dto/responseDto/PaiementResponseDTO.java
back/src/main/java/com/okane/dto/converter/PaiementConverter.java
back/src/main/java/com/okane/service/PaiementService.java
back/src/main/java/com/okane/service/impl/PaiementServiceImpl.java
back/src/main/java/com/okane/service/impl/RecuPaiementPdfServiceImpl.java
back/src/main/java/com/okane/controller/PaiementController.java

# Frontend (5 nouveaux fichiers)
front/src/app/pages/agent/transfer-payment/transfer-payment.component.ts
front/src/app/pages/agent/transfer-payment/transfer-payment.component.html
front/src/app/pages/agent/transfer-payment/transfer-payment.component.css
front/src/app/pages/agent/transfer-payment/services/payment.service.ts
front/src/app/pages/agent/transfer-payment/models/payment.model.ts
```

#### Touchpoints (fichiers existants modifiés)

| Fichier | Modification | Risque conflit |
|---------|-------------|----------------|
| `TransfertRepository.java` | Ajout `findByCodeRetrait()` + `findByBeneficiaireTelephone()` | Faible (additif) |
| `app.routes.ts` | Ajout route `/agent/transfers/payment` | Faible |
| `sidebar.component.ts` | Ajout entrée `WITHDRAWAL` dans navItems | Très faible (additif) |
| Fichiers i18n (`fr.json`, `en.json`, `ar.json`) | Ajout clés page paiement | Très faible |
