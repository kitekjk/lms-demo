import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:lms_mobile_web/core/providers/dio_provider.dart';
import 'package:lms_mobile_web/features/admin/store/data/services/store_api_service.dart';
import 'package:lms_mobile_web/features/admin/store/domain/models/store.dart';

final storeApiServiceProvider = Provider<StoreApiService>((ref) {
  final dio = ref.watch(dioProvider);
  return StoreApiService(dio);
});

final storesProvider = FutureProvider<List<Store>>((ref) async {
  final apiService = ref.watch(storeApiServiceProvider);
  return await apiService.getAllStores();
});

final storeProvider = FutureProvider.family<Store, String>((ref, id) async {
  final apiService = ref.watch(storeApiServiceProvider);
  return await apiService.getStore(id);
});

class StoreNotifier extends StateNotifier<AsyncValue<void>> {
  final StoreApiService _apiService;
  final Ref _ref;

  StoreNotifier(this._apiService, this._ref) : super(const AsyncValue.data(null));

  Future<void> createStore({
    required String name,
    required String location,
  }) async {
    state = const AsyncValue.loading();
    try {
      await _apiService.createStore(name: name, location: location);
      state = const AsyncValue.data(null);
      _ref.invalidate(storesProvider);
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }

  Future<void> updateStore({
    required String id,
    required String name,
    required String location,
  }) async {
    state = const AsyncValue.loading();
    try {
      await _apiService.updateStore(id: id, name: name, location: location);
      state = const AsyncValue.data(null);
      _ref.invalidate(storesProvider);
      _ref.invalidate(storeProvider(id));
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }

  Future<void> deleteStore(String id) async {
    state = const AsyncValue.loading();
    try {
      await _apiService.deleteStore(id);
      state = const AsyncValue.data(null);
      _ref.invalidate(storesProvider);
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }
}

final storeNotifierProvider = StateNotifierProvider<StoreNotifier, AsyncValue<void>>((ref) {
  final apiService = ref.watch(storeApiServiceProvider);
  return StoreNotifier(apiService, ref);
});
