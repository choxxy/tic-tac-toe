# Quick Task Summary: 260402-n4v

## Objective
Fix `java.io.IOException: Not connected` in `GameSocketManager.kt:131` causing fatal crashes.

## Changes
### Network Layer
- Refactored `GameSocketManager.send(message: GameMessage)` to return a `Boolean` status instead of throwing an `IOException`.
- Added `isConnected` property to `GameSocketManager`.
- Updated `GameSocketManager.send` to return `false` if `writer` is `null` or if an exception occurs during sending.

### ViewModel Layer
- Updated `GameViewModel` to handle `send` status for Heartbeats, Moves, and SyncState.
- Updated `LanLobbyViewModel` to handle `send` status for handshakes, throwing a managed `IOException` inside `try-catch` blocks where appropriate (e.g., during connection setup).

### Tests
- Updated `GameSocketManagerTest` to verify `send` return status.
- Added `testSendWhenNotConnectedReturnsFalse` to verify behavior when disconnected.

## Verification
- Ran `./gradlew :app:testDebugUnitTest --tests "com.jna.tictactoe.network.socket.GameSocketManagerTest"` -> **BUILD SUCCESSFUL**.

## Commits
- `67117db` fix(network): make GameSocketManager.send non-throwing and return status
- `954a301` fix(ui): handle send failure gracefully in ViewModels
- `e23db82` test(network): verify GameSocketManager.send status
